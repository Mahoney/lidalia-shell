package uk.org.lidalia.shell

import java.io.BufferedReader
import java.io.Reader
import java.io.StringWriter
import java.util.NoSuchElementException

const val newLine: Int = '\n'.code
const val endOfStream: Int = -1

inline fun Reader.forEachChar(f: (Char) -> Unit) {
  var c: Int
  while (read().also { c = it } != endOfStream) {
    f(c.toChar())
  }
}

fun BufferedReader.readCompleteLine(): String? {
  var c: Int = read()
  return if (c == endOfStream) {
    null
  } else {
    val s = StringWriter()
    do {
      s.append(c.toChar())
    } while (c != newLine && read().also { c = it } != endOfStream)
    s.toString()
  }
}

fun Reader.forEachCompleteLine(action: (String) -> Unit): Unit =
  useCompleteLines { it.forEach(action) }

inline fun <T> Reader.useCompleteLines(block: (Sequence<String>) -> T): T =
  buffered().use { block(it.completeLineSequence()) }

fun BufferedReader.completeLineSequence(): Sequence<String> =
  CompleteLinesSequence(this).constrainOnce()

private class CompleteLinesSequence(private val reader: BufferedReader) : Sequence<String> {
  override fun iterator(): Iterator<String> {
    return object : Iterator<String> {
      private var nextValue: String? = null
      private var done = false

      override fun hasNext(): Boolean {
        if (nextValue == null && !done) {
          nextValue = reader.readCompleteLine()
          if (nextValue == null) done = true
        }
        return nextValue != null
      }

      override fun next(): String {
        if (!hasNext()) {
          throw NoSuchElementException()
        }
        val answer = nextValue
        nextValue = null
        return answer!!
      }
    }
  }
}
