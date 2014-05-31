package com.paypal.stingray.sbt

import sbtrelease.ReleaseStep
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import java.io.PrintWriter
import com.typesafe.sbt.SbtSite.SiteKeys._
import scala.io.Source
import sbtrelease.ReleasePlugin.ReleaseKeys._
import java.text.SimpleDateFormat
import java.util.Calendar

object CustomReleaseStepsKeys {
  lazy val changelog = SettingKey[String]("build-utilities-changelog-file", "Name of the changelog file, default is CHANGELOG.md")
  lazy val readme = SettingKey[String]("build-utilities-readme-file", "Name of the readme file, default is README.md")
  lazy val readmeTemplate = SettingKey[String]("build-utilities-readme-template-file", "Name of the readme template file, default is Readme-Template.md")
}

/**
 * Common methods used amongst custom release step implementations.
 */
trait GetVersion {

  def getReleasedVersion(st: State): String = st.get(versions).getOrElse(
    sys.error("No versions are set! Was this release part executed before inquireVersions?"))._1

}

/**
 * Adds [[https://github.com/sbt/sbt-release sbtrelease]] steps for checking and updating Changelog.
 *
 * Import if defining custom release process and need access to changelog steps.
 */
object ChangelogReleaseSteps extends GetVersion {
  import CustomReleaseStepsKeys._

  case class ChangelogInfo(msg: String, author: String)

  val ChangelogInfoMissingMessage = "You must provide a changelog message and author"
  val ChangelogUpdateMessage = "There was an error writing to the changelog: "
  val ChangelogCommitMessage = "There was an error committing the changelog: "

  class ChangelogInfoMissingException(e: Throwable) extends Exception(e)
  class ChangelogUpdateException(e: Throwable) extends Exception(e)
  class ChangelogCommitException(e: Throwable) extends Exception(e)

  /**
   * Checks to see if mandatory author and message arguments are specified during release.
   */
  lazy val checkForChangelog: ReleaseStep = { st: State =>
    try {
      getChangelogInfo
      st
    } catch {
      case info: ChangelogInfoMissingException => sys.error("You must provide a changelog message and author")
      case e: Throwable => sys.error("There was an error getting the changelog info: " + e.getMessage)
    }
  }

  /**
   * Updates changelog with author and message arguments, then commits changes.
   */
  lazy val updateChangelog: ReleaseStep = { st: State =>
    try {
      val info = getChangelogInfo
      updateChangelog(info, st)
      commitChangelog(st)
      st
    } catch {
      case info: ChangelogInfoMissingException => sys.error(ChangelogInfoMissingMessage)
      case update: ChangelogUpdateException=> sys.error(ChangelogUpdateMessage + update.getMessage)
      case commit: ChangelogCommitException => sys.error(ChangelogCommitMessage + commit.getMessage)
      case e: Throwable => sys.error("There was an error updating the changelog: " + e.getMessage)
    }
  }

  private def getChangelogInfo: ChangelogInfo = {
    try {
      val msg = System.getProperty("changelog.msg")
      val msgExists = Option(msg).exists(_.length > 1)
      val author = System.getProperty("changelog.author")
      val authorExists = Option(author).exists(_.length > 1)
      if (msgExists & authorExists) {
        new ChangelogInfo(msg, author)
      } else {
        throw new Exception("msg or author too short")
      }
    } catch {
      case e: Throwable => throw new ChangelogInfoMissingException(e)
    }
  }

  private def updateChangelog(info: ChangelogInfo, st: State): Unit = {
    try {
      val changelogFile = Project.extract(st).get(changelog)
      val oldChangelog = Source.fromFile(changelogFile).mkString
      val theVersion = getReleasedVersion(st)
      val dateFormat = new SimpleDateFormat("MM/dd/yy")
      val theDate = dateFormat.format(Calendar.getInstance().getTime)

      val out = new PrintWriter( changelogFile, "UTF-8")
      try {
        out.write("\n# " + theVersion + " " + theDate + " released by " + info.author + "\n")
        if (!info.msg.trim.startsWith("*")) out.write("* ")
        out.write(info.msg + "\n")
        oldChangelog.foreach(out.write(_))
      } finally {
        out.close()
      }
    } catch {
      case e: Throwable => throw new ChangelogUpdateException(e)
    }
  }

  private def commitChangelog(st: State): Unit = {
    try {
      val changelogFile = Project.extract(st).get(changelog)
      val vcs = Project.extract(st).get(versionControlSystem).getOrElse(
        sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
      vcs.add(changelogFile) !! st.log
      vcs.commit("Changelog updated for " + getReleasedVersion(st)) ! st.log
    } catch {
      case e: Throwable  => throw new ChangelogCommitException(e)
    }
  }

}

/**
 * Includes a release step `generateReadme` which creates a README.md from the Readme-Template.md template.
 *
 * Looks for a file named `Readme-Template.md`, replaces all instances of {{version}}` with the current release version,
 * and creates `README.md`.
 *
 * Editing the content of the readme should happen via Readme-Template.md.
 *
 * If Readme-Template.md` does not exist, this step will fail.
 */
object ReadmeReleaseSteps extends GetVersion {
  import CustomReleaseStepsKeys._

  val ReadmeGenerateMessage = "There was an error generating the readme: "
  val ReadmeCommitMessage = "There was an error committing the readme: "

  class ReadmeGenerateException(e: Throwable) extends Exception(e)
  class ReadmeCommitException(e: Throwable) extends Exception(e)

  lazy val generateReadme: ReleaseStep = { st: State =>

    try {
      val version = getReleasedVersion(st)
      generateReadmeWithMappings(st, version)
      commitReadme(st, version)
      st
    } catch {
      case generate: ReadmeGenerateException=> sys.error(ReadmeGenerateMessage + generate.getMessage)
      case commit: ReadmeCommitException => sys.error(ReadmeCommitMessage + commit.getMessage)
      case e: Throwable => sys.error("There was an error updating the readme: " + e.getMessage)
    }
  }

  private def generateReadmeWithMappings(st: State, newVersion: String): Unit = {
    try {
      val regex = """\{\{version\}\}""".r
      val extracted = Project.extract(st)
      val readmeFile = extracted.get(readme)
      val templateFile = extracted.get(readmeTemplate)
      val template = Source.fromFile(templateFile).mkString
      val out = new PrintWriter(readmeFile, "UTF-8")
      try {
        val newReadme = regex.replaceAllIn(template, s"$newVersion")
        newReadme.foreach(out.write(_))
      } finally {
        out.close()
      }
    } catch {
      case e: Throwable => throw new ReadmeGenerateException(e)
    }
  }

  private def commitReadme(st: State, newVersion: String): Unit = {
    try {
      val extracted = Project.extract(st)
      val vcs = extracted.get(versionControlSystem).getOrElse(sys.error("Unable to get version control system."))
      val readmeFile = extracted.get(readme)
      vcs.add(readmeFile) !! st.log
      vcs.commit(s"README.md updated to $newVersion") ! st.log
    } catch {
      case e: Throwable => throw new ReadmeCommitException(e)
    }
  }
}

/**
 * Includes a release step `generateAndPushDocs` for [[sbtrelease]] to generate ScalaDocs
 * using sbt-unidoc and sbt-site, followed by pushing them to the gh-pages branch of the current project's repository.
 *
 * Depends on adding `utilitySettings` to root project build settings.
 */
object ScaladocReleaseSteps {

  lazy val generateAndPushDocs: ReleaseStep = { st: State =>
    val st2 = executeTask(makeSite, "Making doc site")(st)
    executeTask(pushSite, "Publishing doc site")(st2)
  }

  private def executeTask(task: TaskKey[_], info: String) = (st: State) => {
    st.log.info(info)
    val extracted = Project.extract(st)
    val ref: ProjectRef = extracted.get(thisProjectRef)
    val (newState, _) = extracted.runTask(task in ref, st)
    newState
  }
}
