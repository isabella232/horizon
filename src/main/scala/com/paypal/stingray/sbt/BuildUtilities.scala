package com.paypal.stingray.sbt

import sbt._
import sbtrelease._
import ReleasePlugin._
import ReleaseStateTransformations._

object BuildUtilities extends Plugin
{

  lazy val defaultStingrayRelease = Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    ChangelogReleaseSteps.checkForChangelog,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    ChangelogReleaseSteps.updateChangelog,
    tagRelease,
    //publishArtifacts,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )

}