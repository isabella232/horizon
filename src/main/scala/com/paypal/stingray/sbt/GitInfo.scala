package com.paypal.stingray.sbt

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git

/**
 * Provides access to Git repository information for the current build.
 *
 * Method `gitProperties` provides a sequence of properties in the form of (key -> value) tuples. See method for usage.
 */
trait GitInfo {

  /**
   * Used to access repository properties for current build.
   *
   * Properties returned include:
   *
   *  - git.branch - Branch name
   *
   *  - git.branch.clean Whether the current branch has uncommitted changes or not
   *
   *  - git.commit.sha - Commit SHA
   *
   *  - git.commit.date - Date and time of commit
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
