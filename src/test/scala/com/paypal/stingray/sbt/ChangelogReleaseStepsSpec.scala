package com.paypal.stingray.sbt

import org.specs2._
import com.paypal.stingray.sbt.ChangelogReleaseSteps._
import java.lang.RuntimeException

/**
 * Tests update and commit release steps in [[com.paypal.stingray.sbt.ChangelogReleaseSteps]]
 */
class ChangelogReleaseStepsSpec extends Specification with ScalaCheck { def is = s2"""
  checkForChangelog without properties should return exception    ${CheckForChangelogNoProps().ok}

"""

  case class CheckForChangelogNoProps() {
    def ok = {
      checkForChangelog.action(null) should throwAn[RuntimeException](ChangelogInfoMissingMessage)
    }
  }

}
