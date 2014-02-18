organization := "com.paypal.stingray"

name := "sbt-build-utilities"

sbtPlugin := true

resolvers += "Stingray Nexus" at "http://stingray-nexus.stratus.dev.ebay.com/nexus/content/groups/public/"

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.2")

publishTo <<= version { version: String =>
  val stingrayNexus = "http://stingray-nexus.stratus.dev.ebay.com/nexus/content/repositories/"
  if (version.trim.endsWith("SNAPSHOT")) {
    Some("snapshots" at stingrayNexus + "snapshots/")
  } else {
    Some("releases" at stingrayNexus + "releases/")
  }
}
