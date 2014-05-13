package com.paypal.stingray.sbt

import sbt._
import sbt.Keys._
import sbtrelease._
import ReleaseStateTransformations._
import com.typesafe.sbt.SbtSite.site
import sbtunidoc.Plugin._
import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import com.typesafe.sbt.SbtGit.GitKeys._
import com.typesafe.sbt.SbtSite.SiteKeys._

/**
 * Primary plugin object used to access all major build utilities.
 * To access add the following import statement:
 *
 * {{{
 *   import com.paypal.stingray.sbt.BuildUtilities._
 * }}}
 *
 */
object BuildUtilities extends GitInfo {

  /**
   * Default release process for projects,
   * uses sequence of release steps from [[https://github.com/sbt/sbt-release sbtrelease]]
   *
   * Depends on adding `docSettings` to build settings in order to complete the `generateAndPushDocs` release step.
   *
   * @example
   * {{{
   *   // in project build file
   *   releaseProcess := defaultReleaseProcess
   * }}}
   *
   * Process order:
   * 1. checkSnapshotDependencies - checks dependencies
   * 2. inquireVersions - gets release version and figures out next version
   * 3. checkForChangelog - checks that system properties
   *    changelog.msg and changelog.author have been set
   * 4. runTest - run tests
   * 5. setReleaseVersion - sets the release version (removes the SNAPSHOT part)
   * 6. commitReleaseVersion - commits the release version
   * 7. updateChangelog - updates the changelog entry for this
   *    release version and commits the change
   * 8. tagRelease - tags the release
   * 9. publishArtifacts - publishes artifacts to specified location
   * 10. generateAndPushDocs - generates Scaladocs and pushes to the gh-pages branch
   * 11. setNextVersion - sets the next snapshot version
   * 12. commitNextVersion - commits the next snapshot version
   * 13. pushChanges - pushes all commits created by this process
   */
  lazy val defaultReleaseProcess = Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    ChangelogReleaseSteps.checkForChangelog,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    ChangelogReleaseSteps.updateChangelog,
    tagRelease,
    publishArtifacts,
    ScaladocReleaseSteps.generateAndPushDocs,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )

  private val gitDir = new File(".", ".git")
  private val repo = new FileRepositoryBuilder().setGitDir(gitDir)
    .readEnvironment() // scan environment GIT_* variables
    .findGitDir() // scan up the file system tree
    .build()
  private val originUrl = repo.getConfig.getString("remote", "origin", "url")

  /**
   * Settings val which provides all settings to generate and publish project documentation to the gh-pages branch of the repository.
   *
   * Combines sbt-unidoc, sbt-site, and ghpages settings. Also sets the following additional settings:
   *
   * gitRemoteRepo (sbt-git) is set to current remote origin, extracted using sbt-git.
   * ghpagesNoJekyll (sbt-ghpages) is set to false
   * siteMappings (sbt-site) is overridden to create a folder structure like /api/$version
   * synchLocal (sbt-ghpages) is overridden so older docs are not deleted
   *
   * Customize settings in your project's build file as needed. For example, look at unidoc settings to exclude aggregate projects from the docs.
   *
   * sbt-site provides commands to create a project site for publishing,
   * and includes commands for auto doc generation. [[https://github.com/sbt/sbt-site]]
   *
   * sbt-unidoc works with sbt-site to combine multiple sub-project documentation into one "site".
   * See docs for how to exclude projects if needed. [[https://github.com/sbt/sbt-unidoc]]
   *
   * ghpages provides commands to easily push a created "site" to the gh-pages branch of the repository. [[https://github.com/sbt/sbt-ghpages]]
   *
   * To use:
   *
   * Add `docSettings` to the root project's settings. For example:
   *
   * {{{
   *   lazy val parent = Project("parent", file("."),
   *    settings = standardSettings ++ docSettings ++ Seq(
   *     name := "parent",
   *    ),
   *    aggregate = Seq(project1, project2)
   *   )
   * }}}
   *
   * Provided you have included `docSettings` in your root project's build settings, the `defaultReleaseProcess` using [[sbtrelease]]
   * includes a step to generate and push docs.
   *
   * To manually create, run the following sbt command:
   *
   * {{{
   *   sbt unidoc make-site ghpages-push-site
   * }}}
   *
   */
  lazy val docSettings: Seq[Setting[_]] =
    unidocSettings ++
    ghpages.settings ++
    site.settings ++ Seq(
      gitRemoteRepo := originUrl,
      ghpagesNoJekyll := false,
      siteMappings <++= (mappings in (ScalaUnidoc, packageDoc), version).map { (mapping, ver) =>
        for((file, path) <- mapping) yield (file, (s"api/$ver/$path"))
      },
      synchLocal <<= (privateMappings, updatedRepository, gitRunner, streams).map { (mappings, repo, git, s) =>
        val betterMappings = mappings.map { case (file, target) => (file, repo / target) }
        IO.copy(betterMappings)
        repo
      }
    )
}


