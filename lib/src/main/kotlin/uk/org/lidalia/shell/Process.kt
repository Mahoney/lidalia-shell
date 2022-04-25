package uk.org.lidalia.shell

import com.github.michaelbull.result.Result
import java.time.Duration

interface Process {
  val pid: Long
  val command: Command
  fun await(timeout: Duration): Result<Succeeded, ProcessState>
  fun await(): Result<Succeeded, Failed>
  fun destroy(): Process
  fun destroyForcibly(): Process
  fun isAlive(): Boolean
  fun info(): ProcessHandle.Info
}
