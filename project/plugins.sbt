resolvers += "Stingray Nexus" at "http://stingray-nexus.stratus.dev.ebay.com/nexus/content/groups/public/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.2" exclude("com.typesafe.sbt", "sbt-git"))

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.0")
