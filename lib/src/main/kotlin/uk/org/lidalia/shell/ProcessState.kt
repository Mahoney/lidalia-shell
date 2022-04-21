package uk.org.lidalia.shell

import uk.org.lidalia.shell.ExitStatus.Companion.SUCCESS

class ExitStatus private constructor(val status: Int) {

  val isSuccess = status == 0

  companion object {
    val SUCCESS = ExitStatus(0)
    operator fun invoke(status: Int): ExitStatus {
      require(status >= 0)
      return if (status == 0) SUCCESS
      else ExitStatus(status)
    }
  }

  override fun equals(other: Any?): Boolean =
    this === other || (other is ExitStatus && other.status == status)

  override fun hashCode(): Int = status
  override fun toString(): String = status.toString()
}

sealed class ProcessState {

  abstract val command: Command
  abstract val status: ExitStatus?
  abstract val stdout: String
  abstract val stderr: String
  abstract val output: String
  abstract val isSuccess: Boolean

  final override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ProcessState) return false

    if (command != other.command) return false
    if (status != other.status) return false
    if (stdout != other.stdout) return false
    if (stderr != other.stderr) return false

    return true
  }

  final override fun hashCode(): Int {
    var result = command.hashCode()
    result = 31 * result + (status?.hashCode() ?: -1)
    result = 31 * result + stdout.hashCode()
    result = 31 * result + stderr.hashCode()
    return result
  }
}

sealed class Completed : ProcessState() {
  abstract override val status: ExitStatus

  companion object {
    operator fun invoke(
      command: Command,
      status: ExitStatus,
      stdout: String,
      stderr: String,
      combinedOutput: String
    ): Completed = if (status.isSuccess) {
      Succeeded(command, stdout, stderr, combinedOutput)
    } else {
      Failed(command, status, stdout, stderr, combinedOutput)
    }
  }
}

class Succeeded(
  override val command: Command,
  override val stdout: String,
  override val stderr: String,
  override val output: String
) : Completed() {

  override val status: ExitStatus = SUCCESS
  override val isSuccess: Boolean = true

  override fun toString(): String =
    "Success(command='$command', output='$output')"
}

class Failed(
  override val command: Command,
  override val status: ExitStatus,
  override val stdout: String,
  override val stderr: String,
  override val output: String
) : Completed() {
  init {
    require(status != SUCCESS) { "status [$status] must not be $SUCCESS" }
  }

  override val isSuccess: Boolean = false

  override fun toString(): String =
    "Failure(command='$command', status=$status, output='$output')"
}

class Incomplete(
  override val command: Command,
  override val stdout: String,
  override val stderr: String,
  override val output: String
) : ProcessState() {

  override val status: ExitStatus? = null

  override val isSuccess: Boolean = false

  override fun toString(): String =
    "Incomplete(command='$command', output='$output')"
}
