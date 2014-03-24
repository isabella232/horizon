package com.paypal.stingray.sbt

import org.specs2._

/**
 * Tests [[com.paypal.stingray.sbt.GitInfo]] trait
 */
class GitInfoSpecs extends Specification { def is = s2"""

  gitProperties correctly returns keys and info                 ${GitProps().ok}

"""

  case class GitProps() {
    case class Info() extends GitInfo
    def ok = {
      val repo = new Info()
      val repoInfo = repo.gitProperties
      (repoInfo.length must beEqualTo(4)) and
        (repoInfo must haveKeys[String]("git.branch", "git.branch.clean", "git.commit.sha", "git.commit.date"))
    }
  }

}
