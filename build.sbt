organization := "com.paypal.stingray"

name := "sbt-build-utilities"

version := "0.1.0"

sbtPlugin := true

resolvers += "Stingray Nexus" at "http://stingray-nexus.stratus.dev.ebay.com/nexus/content/groups/public/"

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.2")
