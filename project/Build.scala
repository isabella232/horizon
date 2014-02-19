import sbt._
import sbt.Keys._
import scala.Some
import scala.Some

object UtilitiesBuild extends Build {

  val stingrayNexusHost = "http://stingray-nexus.stratus.dev.ebay.com"

  lazy val root = Project(id = "root", base = file(".")).settings(
    organization := "com.paypal.stingray",
    name := "sbt-build-utilities",
    version := "0.1-SNAPSHOT",
    sbtPlugin := true,
    resolvers += "Stingray Nexus" at s"$stingrayNexusHost/nexus/content/groups/public/",
    addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.2"),
    publishTo <<= (version) { version: String =>
      val stingrayNexus = s"$stingrayNexusHost/nexus/content/repositories/"
      val publishFolder = if(version.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"
      Some(publishFolder at stingrayNexus + s"$publishFolder/")
    }
  )

}