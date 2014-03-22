package com.paypal.stingray.sbt

import sbtrelease._
import ReleaseStateTransformations._
import sbt._
import sbt.Def
import sbt.Keys._

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

}
