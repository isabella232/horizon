package com.paypal.stingray.sbt

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtGit.GitKeys._
import com.typesafe.sbt.SbtGit.GitKeys
import com.typesafe.sbt.git.GitRunner

/**
 * Provides access to Git repository information for the current build.
 */
trait GitInfo {

  /**
   * Used to access repository properties for current build.
   *
   * Properties returned include:
   *  Branch name
   *  Whether the current branch has uncommitted changes or not
   *  Commit SHA
   *  Date and time of commit
   *
   * @return a sequence of properties for the current repository
   */
  def gitProperties: Seq[(String, String)] = {
    val repo = (new FileRepositoryBuilder).findGitDir.build
    val git = new Git(repo)
    val status = git.status.call

    val repoInfo = Seq(
      "git.branch" -> repo.getBranch,
      "git.branch.clean" -> status.isClean.toString
    )

    val commit = Option(git.log.call.iterator) match {
      case Some(i) if i.hasNext => Some(i.next)
      case _ => None
    }

    commit match {
      case Some(c) =>
        val author = c.getAuthorIdent
        repoInfo ++ Seq(
          "git.commit.sha" -> c.name,
          "git.commit.date" -> author.getWhen.toString
        )
      case None => repoInfo
    }
  }

}

object GhPagesKeys {
  lazy val repository = SettingKey[File]("ghpages-repository", "sandbox environment where git project ghpages branch is checked out.")
  lazy val ghpagesNoJekyll = SettingKey[Boolean]("ghpages-no-jekyll", "If this flag is set, ghpages will automatically generate a .nojekyll file to prevent github from running jekyll on pushed sites.")
  lazy val updatedRepository = TaskKey[File]("ghpages-updated-repository", "Updates the local ghpages branch on the sandbox repository.")
  // Note:  These are *only* here in the event someone wants to completely bypass the sbt-site plugin.
  lazy val privateMappings = mappings in synchLocal
  lazy val synchLocal = TaskKey[File]("ghpages-synch-local", "Copies the locally generated site into the local ghpages repository.")
  lazy val cleanSite = TaskKey[Unit]("ghpages-clean-site", "Cleans the staged repository for ghpages branch.")
  lazy val pushSite = TaskKey[Unit]("ghpages-push-site", "Pushes a generated site into the ghpages branch.  Will not clean the branch unless you run clean-site first.")
  lazy val gitBranch = SettingKey[Option[String]]("git-branch", "Target branch of a git operation")
}


object ghpages {
  import GhPagesKeys._
  import com.typesafe.sbt.SbtSite.SiteKeys._

  val repo = (new FileRepositoryBuilder).findGitDir.build

  val origin = repo.getDirectory.toString

  val config = repo.getConfig
  val originUrl = config.getString("remote", "origin", "url")

  lazy val settings: Seq[Setting[_]] = Seq(
    gitRemoteRepo := originUrl,
    ghpagesNoJekyll := false,
    repository <<= (name,organization) apply ((n,o) => file(System.getProperty("user.home")) / ".sbt" / "ghpages" / o / n),
    gitBranch in updatedRepository <<= gitBranch ?? Some("gh-pages"),
    updatedRepository <<= updatedRepo(repository, gitRemoteRepo, gitBranch in updatedRepository),
    pushSite <<= pushSite0,
    privateMappings <<= siteMappings,
    synchLocal <<= synchLocal0,
    cleanSite <<= cleanSite0
  )
  private def updatedRepo(repo: SettingKey[File], remote: SettingKey[String], branch: SettingKey[Option[String]]) =
    (repo, remote, branch, GitKeys.gitRunner, streams) map { (local, uri, branch, git, s) =>
      git.updated(remote = uri, cwd = local, branch = branch, log = s.log);
      local
    }

  private def synchLocal0 = (privateMappings, updatedRepository, ghpagesNoJekyll, GitKeys.gitRunner, streams) map { (mappings, repo, noJekyll, git, s) =>
    val betterMappings = mappings map { case (file, target) => (file, repo / target) }
    // First, remove 'stale' files.
    cleanSiteForRealz(repo, git, s)
    // Now copy files.
    IO.copy(betterMappings)
    if(noJekyll) IO.touch(repo / ".nojekyll")
    repo
  }

  private def cleanSite0 = (updatedRepository, GitKeys.gitRunner, streams) map { (dir, git, s) =>
    println("ORIGIN IS " + origin)
    println("CONFIG IS " + config)
    println("URL IS " + originUrl)
    cleanSiteForRealz(dir, git, s)
  }
  private def cleanSiteForRealz(dir: File, git: GitRunner, s: TaskStreams): Unit = {
    val toClean = IO.listFiles(dir).filterNot(_.getName == ".git").map(_.getAbsolutePath).toList
    if(!toClean.isEmpty)
      git(("rm" :: "-r" :: "-f" :: "--ignore-unmatch" :: toClean) :_*)(dir, s.log)
    ()
  }

  val commitMessage = sys.env.getOrElse("SBT_GHPAGES_COMMIT_MESSAGE", "updated site")
  private def pushSite0 = (synchLocal, GitKeys.gitRunner, streams) map { (repo, git, s) => git.commitAndPush(commitMessage)(repo, s.log) }

}
