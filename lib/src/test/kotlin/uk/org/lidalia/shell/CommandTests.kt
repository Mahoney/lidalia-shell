package uk.org.lidalia.shell

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.InputStream
import java.io.StringWriter

@Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
class CommandTests : StringSpec({

  "returns result with no new line" {
    "echo 'hello world'"() shouldBe "hello world"
  }

  "returns piped result" {
    ("printf 'hello world'" `|` "cat")() shouldBe "hello world"
  }

  "throws exception on fail" {
    val e = shouldThrow<ProcessFailedException> {
      "printf 'pre fail' && false && printf 'post fail'"()
    }
    e.message shouldBe e.failure.toString()
    e.failure shouldBe Failed(
      command = Shell("printf 'pre fail' && false && printf 'post fail'"),
      status = ExitStatus(1),
      stdout = "pre fail",
      stderr = "",
      output = "pre fail",
    )
  }

  "throws exception on pipe fail" {

    val e = shouldThrow<ProcessFailedException> {
      ("exit 1" `|` "echo 'hello world'")()
    }
    e.message shouldBe e.failure.toString()
    e.failure shouldBe Failed(
      command = Shell("exit 1"),
      status = ExitStatus(1),
      stdout = "",
      stderr = "",
      output = "",
    )
  }

  "can recreate string ending in line feed" {
    val inputWithLineFeed = "foo\nbar\n"
    inputWithLineFeed.byteInputStream().reconstituted() shouldBe inputWithLineFeed
  }

  "can recreate string ending without line feed" {

    val inputWithoutLineFeed = "foo\nbar"
    inputWithoutLineFeed.byteInputStream().reconstituted() shouldBe inputWithoutLineFeed
  }
})

fun InputStream.reconstituted(): String {
  val s: Appendable = StringWriter()
  reader().forEachCompleteLine { line ->
    s.append(line)
  }
  return s.toString()
}
