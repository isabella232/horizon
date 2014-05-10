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
import com.typesafe.sbt.SbtGit._
import com.typesafe.sbt.SbtGit.GitKeys._
import com.typesafe.sbt.SbtSite
import com.typesafe.sbt.SbtSite.SiteKeys._
import sbtrelease.ReleasePlugin.ReleaseKeys._


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
   * 3. ChangelogReleaseSteps.checkForChangelog - checks that system properties
   *    changelog.msg and changelog.author have been set
   * 4. runTest - run tests
   * 5. setReleaseVersion - sets the release version (removes the SNAPSHOT part)
   * 6. commitReleaseVersion - commits the release version
   * 7. ChangelogReleaseSteps.updateChangelog - updates the changelog entry for this
   *    release version and commits the change
   * 8. tagRelease - tags the release
   * 9. publishArtifacts - publishes artifacts to specified location
   * 10. generateAndPushDocs - generates Scaladocs and pushes changes to the gh-pages branch
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

  lazy private val gitDir = new File(".", ".git")
  lazy private val repo = new FileRepositoryBuilder().setGitDir(gitDir)
    .readEnvironment() // scan environment GIT_* variables
    .findGitDir() // scan up the file system tree
    .build()
  lazy private val originUrl = repo.getConfig.getString("remote", "origin", "url")

  // TODO revise
  /**
   * Settings val which provides all settings to generate and publish project documentation to the gh-pages branch of the repository.
   *
   * Combines sbt-site, sbt-unidoc and custom ghpages settings. Customize settings in your project's build file as needed.
   *
   * sbt-site provides commands to create a project site for publishing,
   * and includes commands for auto doc generation. [[https://github.com/sbt/sbt-site]]
   *
   * sbt-unidoc works with sbt-site to combine multiple sub-project documentation into one "site".
   * See docs for how to exclude projects if needed. [[https://github.com/sbt/sbt-unidoc]]
   *
   * ghpages provides commands to easily push a created "site" to the gh-pages branch of the repository. [[https://github.com/sbt/sbt-ghpages]]
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
   * To generate and push docs, run the following sbt command:
   *
   * {{{
   *   sbt ghpages-push-site
   * }}}
   *
   * Alternatively, the `defaultReleaseProcess` using [[sbtrelease]] will automatically generate and push docs,
   * provided `docSettings` is included in the build settings.
   */
  lazy val docSettings: Seq[Setting[_]] =
    unidocSettings ++
    ghpages.settings ++
    site.settings ++ Seq(
      gitRemoteRepo := originUrl,
      ghpagesNoJekyll := false,
      siteMappings <++= (mappings in (ScalaUnidoc, packageDoc), version) map { (m, v) =>
        for((f, d) <- m) yield (f, ("api/"+v+"/"+d))
      },
      synchLocal <<= (privateMappings, updatedRepository, gitRunner, streams) map { (mappings, repo, git, s) =>
        val betterMappings = mappings map { case (file, target) => (file, repo / target) }
        IO.copy(betterMappings)
        repo
      }
    )

  lazy val testReleaseProcess = Seq[ReleaseStep](
    ScaladocReleaseSteps.generateAndPushDocs
  )

}


