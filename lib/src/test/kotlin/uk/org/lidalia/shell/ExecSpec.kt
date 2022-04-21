// ktlint-disable indent https://github.com/pinterest/ktlint/issues/967

package uk.org.lidalia.shell

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe

class ExecSpec : StringSpec({

  forAll(
    table(
      /* ktlint-disable no-multi-spaces */
      headers("exec",                             "expected command"),
      row(Exec("foo"),                            """foo"""),
      row(Exec("echo", """hello""", """world"""), """echo hello world"""),
      row(Exec("echo", """hello world"""),        """echo 'hello world'"""),
      row(Exec("echo", """hello ' world"""),      """echo 'hello '\'' world'"""),
      row(Exec("echo", """hello '\'' world"""),   """echo 'hello '\''\'\'''\'' world'"""),
      /* ktlint-enable no-multi-spaces */
    )
  ) { exec, expectedCommand ->
    "safely prints $exec  as [$expectedCommand]" {
      exec.command shouldBe expectedCommand
    }
  }
})
