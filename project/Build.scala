import com.typesafe.sbt.SbtSite.SiteKeys._
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import sbt._
import Keys._
import sbtunidoc.Plugin._
import com.typesafe.sbt.SbtSite._
import com.typesafe.sbt.SbtGit._
import GitKeys._
import com.typesafe.sbt.SbtGhPages._
import GhPagesKeys._
import sbtrelease._
import scala.io.Source
import net.virtualvoid.sbt.graph.Plugin
import org.scalastyle.sbt.ScalastylePlugin
import de.johoop.jacoco4sbt._
import JacocoPlugin._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

object BuildSettings {
  import AdditionalReleaseSteps._

  val org = "com.paypal.stingray"
  val scalaVsn = "2.10.4"
  val stingrayNexusHost = "http://stingray-nexus.stratus.dev.ebay.com"
  private val gitDir = new File(".", ".git")
  private val repo = new FileRepositoryBuilder().setGitDir(gitDir)
    .readEnvironment() // scan environment GIT_* variables
    .findGitDir() // scan up the file system tree
    .build()
  private val originUrl = repo.getConfig.getString("remote", "origin", "url")

  lazy val standardPluginSettings = Defaults.defaultSettings ++
    releaseSettings ++
    Plugin.graphSettings ++
    ScalastylePlugin.Settings ++
    jacoco.settings ++
    site.settings ++
    ghpages.settings ++
    unidocSettings ++
    Seq(
      ghpagesNoJekyll := false,
      siteMappings <++= (mappings in (ScalaUnidoc, packageDoc), version).map { (mapping, ver) =>
        for((file, path) <- mapping) yield (file, (s"api/$ver/$path"))
      },
      synchLocal <<= (privateMappings, updatedRepository, gitRunner, streams).map { (mappings, repo, git, s) =>
        val betterMappings = mappings.map { case (file, target) => (file, repo / target) }
        IO.copy(betterMappings)
        repo
      },
      git.remoteRepo := originUrl,
      tagName <<= (version in ThisBuild).map(a => a),
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        ensureChangelogEntry,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishArtifacts,
        generateAndPushDocs,
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    )

  lazy val standardSettings = standardPluginSettings ++ Seq(
    organization := org,
    name := "sbt-build-utilities",
    scalaVersion := scalaVsn,
    sbtPlugin := true,
    conflictManager := ConflictManager.strict,
    fork := true,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    resolvers += "Stingray Nexus" at s"$stingrayNexusHost/nexus/content/groups/public/",
    dependencyOverrides <++= scalaVersion { vsn => Set(
      "org.scala-lang" % "scala-library"  % vsn,
      "org.scala-lang" % "scala-compiler" % vsn
    )},
    addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.2"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.2" exclude("com.typesafe.sbt", "sbt-git")),
    addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.7.0"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.5-stingray"),
    addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.0"),
    libraryDependencies ++= Seq(
      "org.eclipse.jgit" % "org.eclipse.jgit" % "3.3.0.201403021825-r",
      "org.specs2" %% "specs2" % "2.3.11" % "test"
    ),
    publishTo <<= version { version: String =>
      val stingrayNexus = s"$stingrayNexusHost/nexus/content/repositories/"
      val publishFolder = if(version.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"
      Some(publishFolder at stingrayNexus + s"$publishFolder/")
    }
  )
}

object UtilitiesBuild extends Build {
  import BuildSettings._

  lazy val root = Project(id = "root", base = file("."), settings = standardSettings)

}

/**
 * Adds step to ensure an entry for the current release version is present in the changelog,
 * and generate and push ScalaDocs to gh-pages branch.
 */
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

  private def getReleasedVersion(st: State): String = st.get(versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))._1

  private def checkChangelog(st: State) {
    try {
      val currentChangelog = Source.fromFile(changelog).mkString
      val version = getReleasedVersion(st)
      if (!currentChangelog.contains(version)) {
        throw new Exception(s"No changelog entry found for current release version $version.")
      }
    } catch {
      case e: Throwable => throw new ChangelogEntryMissingException(e)
    }
  }

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
