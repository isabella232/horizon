package com.paypal.stingray.sbt

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git

/**
 *
 */
trait GitInfo {

  /**
   *
   * @return a sequence
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
      "git.commit.time" -> author.getWhen.toString
    )
  }
}
