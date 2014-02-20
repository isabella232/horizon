import sbt._
import sbt.Keys._
import sbtrelease._
import scala.io.Source
import scala.Some
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

object BuildSettings {
  import AdditionalReleaseSteps._

  val stingrayNexusHost = "http://stingray-nexus.stratus.dev.ebay.com"

  lazy val standardSettings = Defaults.defaultSettings ++ releaseSettings ++ Seq(
    organization := "com.paypal.stingray",
    name := "sbt-build-utilities",
    sbtPlugin := true,
    conflictManager := ConflictManager.strict,
    fork := true,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    resolvers += "Stingray Nexus" at s"$stingrayNexusHost/nexus/content/groups/public/",
    addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.2"),
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2" % "2.3.8" % "test"
    ),
    publishTo <<= (version) { version: String =>
      val stingrayNexus = s"$stingrayNexusHost/nexus/content/repositories/"
      val publishFolder = if(version.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"
      Some(publishFolder at stingrayNexus + s"$publishFolder/")
    },
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      ensureChangelogEntry,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}

object UtilitiesBuild extends Build {
  import BuildSettings._

  lazy val root = Project(id = "root", base = file("."), settings = standardSettings)

}

// Adds step to ensure an entry for the current release version is present in the changelog
object AdditionalReleaseSteps {

  lazy val ensureChangelogEntry: ReleaseStep = { st: State =>

    try {
      checkChangelog(st)
      st
    } catch {
      case entry: ChangelogEntryMissingException => sys.error(entry.getMessage)
    }
  }

  val changelog = "CHANGELOG.md"

  class ChangelogEntryMissingException(e: Throwable) extends Exception(e)

  private def getReleasedVersion(st: State) = st.get(versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))._1

  private def checkChangelog(st: State) = {
    try {
      val currentChangelog = Source.fromFile(changelog).mkString
      println("current changelog is: " + currentChangelog)
      val version = getReleasedVersion(st)
      println("current version is " + version)
      if (! currentChangelog.contains(s"# $version ")) {
        throw new Exception(s"No changelog entry found for current release version $version.")
      }
    } catch {
      case e: Throwable => throw new ChangelogEntryMissingException(e)
    }
  }

}
