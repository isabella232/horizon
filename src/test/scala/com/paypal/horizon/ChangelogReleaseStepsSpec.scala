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
package com.paypal.horizon

import org.specs2._
import com.paypal.horizon.ChangelogReleaseSteps._
import java.lang.RuntimeException

/**
 * Tests update and commit release steps in [[com.paypal.horizon.sbt.ChangelogReleaseSteps]]
 */
class ChangelogReleaseStepsSpec extends Specification with ScalaCheck { override def is = s2"""
  checkForChangelog without properties should return exception    ${CheckForChangelogNoProps().ok}

"""

  case class CheckForChangelogNoProps() {
    def ok = {
      def msg = "You must provide a changelog message and author"
      checkForChangelog.action(null) should throwAn[RuntimeException](msg)
    }
  }

}
