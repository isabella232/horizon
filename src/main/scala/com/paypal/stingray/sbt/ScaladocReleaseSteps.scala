package com.paypal.stingray.sbt

import sbtrelease.ReleaseStep
import sbt.{Project, State}
import sbt.Keys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._

/**
 * Includes a release step `generateAndPushDocs` for [[sbtrelease]] to generate Scaladocs
 * and push them to the gh-pages branch of the current project's repository.
 *
 * Depends on adding `docSettings` to build settings.
 */
object ScaladocReleaseSteps {

  lazy val generateAndPushDocs: ReleaseStep = { st: State =>
    val extracted = Project.extract(st)
    val thisRef = extracted.get(thisProjectRef)
    extracted.runTask(pushSite in thisRef, st)
    st
  }

}
