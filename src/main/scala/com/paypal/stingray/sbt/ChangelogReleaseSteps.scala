package com.paypal.stingray.sbt

import sbt._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStep
import scala.io.Source
import java.text.SimpleDateFormat
import java.util.Calendar
import java.io.PrintWriter

/**
 * Adds [[https://github.com/sbt/sbt-release sbtrelease]] steps for checking and updating Changelog.
 *
 * Import if defining custom release process and need access to changelog steps.
 */
object ChangelogReleaseSteps {
  val changelog = "CHANGELOG.md"

  private def getReleasedVersion(st: State): String = st.get(versions).getOrElse(
    sys.error("No versions are set! Was this release part executed before inquireVersions?"))._1

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
      val oldChangelog = Source.fromFile(changelog).mkString
      val theVersion = getReleasedVersion(st)
      val dateFormat = new SimpleDateFormat("MM/dd/yy")
      val theDate = dateFormat.format(Calendar.getInstance().getTime)

      val out = new PrintWriter( changelog, "UTF-8")
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
      val vcs = Project.extract(st).get(versionControlSystem).getOrElse(
        sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
      vcs.add(changelog) !! st.log
      vcs.commit("Changelog updated for " + getReleasedVersion(st)) ! st.log
    } catch {
      case e: Throwable  => throw new ChangelogCommitException(e)
    }
  }

}
