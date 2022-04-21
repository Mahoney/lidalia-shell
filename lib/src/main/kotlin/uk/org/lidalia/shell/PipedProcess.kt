package uk.org.lidalia.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import java.time.Duration

class PipedProcess(
  override val command: Command,
  val processes: List<Process>
) : Process {

  override val pid: Long = processes.last().pid

  override fun await(timeout: Duration): Result<Succeeded, ProcessState> {
    val results = processes.map { it.await(timeout) }
    return results.firstOrNull { it is Err } ?: results.last()
  }

  override fun await(): Result<Succeeded, Failed> {
    val results = processes.map { it.await() }
    return results.firstOrNull { it is Err } ?: results.last()
  }

  override fun destroy(): Process {
    processes.forEach { it.destroy() }
    return this
  }

  override fun destroyForcibly(): Process {
    processes.forEach { it.destroyForcibly() }
    return this
  }

  override fun isAlive(): Boolean {
    return processes.any { it.isAlive() }
  }

  override fun info(): ProcessHandle.Info {
    TODO("Not yet implemented")
  }
}
