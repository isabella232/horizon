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

import org.specs2._

/**
 * Tests [[com.paypal.horizon.sbt.GitInfo]] trait
 */
class GitInfoSpecs extends Specification { override def is = s2"""

  gitProperties correctly returns keys and info                 ${GitProps().ok}

"""

  case class GitProps() {
    def ok = {
      val repo = new GitInfo { }
      val repoInfo = repo.gitProperties
      (repoInfo.length must beEqualTo(4)) and
        (repoInfo must haveKeys[String]("git.branch", "git.branch.clean", "git.commit.sha", "git.commit.date"))
    }
  }

}
