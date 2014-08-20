/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.horizon.sbt

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
