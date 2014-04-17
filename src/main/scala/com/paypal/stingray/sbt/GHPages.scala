package com.paypal.stingray.sbt

import sbt._
import sbt.Keys._
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import com.typesafe.sbt.SbtGit.GitKeys._
import com.typesafe.sbt.SbtGit.GitKeys
import com.typesafe.sbt.git.GitRunner

object GhPagesKeys {
  lazy val repository = SettingKey[File]("ghpages-repository", "sandbox environment where git project ghpages branch is checked out.")
  lazy val updatedRepository = TaskKey[File]("ghpages-updated-repository", "Updates the local ghpages branch on the sandbox repository.")
  lazy val privateMappings = mappings in syncLocal
  lazy val syncLocal = TaskKey[File]("ghpages-sync-local", "Copies the locally generated site into the local ghpages repository.")
  lazy val cleanSite = TaskKey[Unit]("ghpages-clean-site", "Cleans the staged repository for ghpages branch.")
  lazy val pushSite = TaskKey[Unit]("ghpages-push-site", "Pushes a generated site into the ghpages branch.  Will not clean the branch unless you run clean-site first.")
  lazy val gitBranch = SettingKey[Option[String]]("git-branch", "Target branch of a git operation")
}


object ghpages {
  import GhPagesKeys._
  import com.typesafe.sbt.SbtSite.SiteKeys._

  val repo = (new FileRepositoryBuilder).findGitDir.build
  val origin = repo.getDirectory.toString
  val originUrl = repo.getConfig.getString("remote", "origin", "url")

  lazy val settings: Seq[Setting[_]] = Seq(
    gitRemoteRepo := originUrl,
    repository <<= (name,organization) apply ((n,o) => file(System.getProperty("user.home")) / ".sbt" / "ghpages" / o / n),
    gitBranch in updatedRepository <<= gitBranch ?? Some("gh-pages"),
    updatedRepository <<= updatedRepo(repository, gitRemoteRepo, gitBranch in updatedRepository),
    pushSite <<= pushSite0,
    privateMappings <<= siteMappings,
    syncLocal <<= syncLocal0,
    cleanSite <<= cleanSite0
  )
  private def updatedRepo(repo: SettingKey[File], remote: SettingKey[String], branch: SettingKey[Option[String]]) =
    (repo, remote, branch, GitKeys.gitRunner, streams) map { (local, uri, branch, git, s) =>
      git.updated(remote = uri, cwd = local, branch = branch, log = s.log);
      local
    }

  private def syncLocal0 = (privateMappings, updatedRepository, GitKeys.gitRunner, streams) map { (mappings, repo, git, s) =>
    val betterMappings = mappings map { case (file, target) => (file, repo / target) }
    // First, remove 'stale' files.
    cleanSiteForRealz(repo, git, s)
    // Now copy files.
    IO.copy(betterMappings)
    repo
  }

  private def cleanSite0 = (updatedRepository, GitKeys.gitRunner, streams) map { (dir, git, s) =>
    cleanSiteForRealz(dir, git, s)
  }
  private def cleanSiteForRealz(dir: File, git: GitRunner, s: TaskStreams): Unit = {
    val toClean = IO.listFiles(dir).filterNot(_.getName == ".git").map(_.getAbsolutePath).toList
    if(!toClean.isEmpty)
      git(("rm" :: "-r" :: "-f" :: "--ignore-unmatch" :: toClean) :_*)(dir, s.log)
    ()
  }

  val commitMessage = sys.env.getOrElse("SBT_GHPAGES_COMMIT_MESSAGE", "updated site")
  private def pushSite0 = (syncLocal, GitKeys.gitRunner, streams) map { (repo, git, s) => git.commitAndPush(commitMessage)(repo, s.log) }

}
