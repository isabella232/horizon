package com.paypal.stingray.sbt

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git

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
    val commit = git.log.call.iterator.next
    val author = commit.getAuthorIdent
    Seq(
      "git.branch" -> repo.getBranch,
      "git.branch.clean" -> status.isClean.toString,
      "git.commit.sha" -> commit.name,
      "git.commit.date" -> author.getWhen.toString
    )
  }
}
