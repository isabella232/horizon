package com.paypal.stingray.sbt

import sbtrelease.ReleaseStep
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.SbtGit.git
import java.io.File
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import com.typesafe.sbt.SbtSite.SiteKeys._

/**
 * Includes a release step `generateAndPushDocs` for [[sbtrelease]] to generate Scaladocs
 * using sbt-unidoc and sbt-site, followed by pushing them to the gh-pages branch of the current project's repository.
 *
 * Depends on adding `docSettings` to root project build settings.
 */
object ScaladocReleaseSteps {

  lazy val generateAndPushDocs: ReleaseStep = { st: State =>
    val st2 = executeTask(makeSite, "Making the doc site")(st)
    executeTask(pushSite, "Publishing the doc site")(st2)
  }

  private def executeTask(task: TaskKey[_], info: String) = (st: State) => {
    st.log.info(info)
    val extracted = Project.extract(st)
    val ref: ProjectRef = extracted.get(thisProjectRef)
    extracted.runTask(task in ref, st)._1
  }
}
