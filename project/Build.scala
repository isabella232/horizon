import sbt._
import sbt.Keys._
import sbtrelease._
import scala.Some
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

object UtilitiesBuild extends Build {

  val stingrayNexusHost = "http://stingray-nexus.stratus.dev.ebay.com"

  lazy val root = Project(id = "root", base = file(".")).settings(
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
