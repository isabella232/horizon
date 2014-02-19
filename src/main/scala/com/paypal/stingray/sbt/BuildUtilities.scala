package com.paypal.stingray.sbt

import sbt._
import sbtrelease._
import ReleaseStateTransformations._

/**
 * Primary plugin object used to access all major build utilities.
 */
object BuildUtilities extends Plugin
{
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

}