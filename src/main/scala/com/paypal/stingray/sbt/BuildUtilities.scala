package com.paypal.stingray.sbt

import sbt._
import sbtrelease._
import ReleaseStateTransformations._
import com.typesafe.sbt.SbtSite.site
import sbtunidoc.Plugin._


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
   * Default release process for Stingray projects,
   * in the form of a sequence of [[https://github.com/sbt/sbt-release sbtrelease]] release steps
   *
   * @example
   * {{{
   *    // in project build file
   *   releaseProcess := defaultStingrayRelease
   * }}}
   */
  lazy val defaultStingrayRelease = Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    ChangelogReleaseSteps.checkForChangelog,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    ChangelogReleaseSteps.updateChangelog,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )

  /**
   * Settings val which provides all settings to generate and publish project documentation to the gh-pages branch of the repository.
   *
   * Combines sbt-site, sbt-unidoc and custom ghpages settings. Customize settings in your project's build file as needed.
   *
   * sbt-site provides commands to create a project site for publishing,
   * and includes commands for auto doc generation: [[https://github.com/sbt/sbt-site]]
   *
   * sbt-unidoc works with sbt-site to combine multiple sub-project documentation into one "site": [[https://github.com/sbt/sbt-unidoc]]
   *
   * ghpages provides commands to easily push a created "site" to the gh-pages branch of the repository.
   *
   * To use in a project with no sub-projects:
   *
   * 1. add docSettings to your sequence of build settings
   * 2. run sbt ghpages-push-site from the command line
   *
   * To use to unify docs for multiple sub-projects within a project:
   *
   * 1. add docSettings and site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "latest/api")
   *    to the parent project's build settings. For example,
   *
   * {{{
   *   lazy val parent = Project("parent", file("."),
   *    settings = standardSettings ++ docSettings ++ Seq(
   *     site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "latest/api"),
   *     name := "parent",
   *    ),
   *    aggregate = Seq(project1, project2)
   *   )
   * }}}
   *
   * 3. run sbt unidoc ghpages-push-site from the command line
   */
  lazy val docSettings: Seq[Setting[_]] = site.settings ++ ghpages.settings ++ unidocSettings

}


