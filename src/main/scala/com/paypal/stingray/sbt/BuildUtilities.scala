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
   * Settings val which combines sbt-site, sbt-unidoc and custom ghpages settings.
   * Customize settings in your project's build file as needed.
   *
   * sbt-site provides commands to create a site folder for publishing,
   * and easily include docs: [[https://github.com/sbt/sbt-site]]
   *
   * sbt-unidoc works with sbt-site to combine multiple sub-project documentation into one "site": [[https://github.com/sbt/sbt-unidoc]]
   *
   * ghpages provides commands to easily push a created site folder to the gh-pages branch of the repository.
   */
  lazy val docSettings: Seq[Setting[_]] = site.settings ++ ghpages.settings ++ unidocSettings

}


