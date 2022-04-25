package uk.org.lidalia.shell

import java.lang.IllegalArgumentException
import java.nio.file.Path

object JavaProcessStarter : ProcessStarter {
  override fun run(
    command: CommandInContext,
    outStream: Appendable,
    errStream: Appendable,
  ): Process = when (command.command) {
    is Exec -> command.command.run(command.dir, command.env, outStream, errStream)
    is Shell -> command.command.run(command.dir, command.env, outStream, errStream)
    is Pipe -> command.command.run(command.dir, command.env, outStream, errStream)
  }
}

private fun Exec.run(
  dir: Path,
  env: Map<String, String>,
  outStream: Appendable,
  errStream: Appendable,
) = run(
  toProcessBuilder(),
  dir,
  env,
  outStream,
  errStream,
)

private fun Shell.run(
  dir: Path,
  env: Map<String, String>,
  outStream: Appendable,
  errStream: Appendable,
) = run(
  toProcessBuilder(),
  dir,
  env,
  outStream,
  errStream,
)

private fun Pipe.run(
  dir: Path,
  env: Map<String, String>,
  outStream: Appendable,
  errStream: Appendable,
): PipedProcess {
  val flatCommands = commands.flatMap { it.flatten() }
  val processBuilders = flatCommands.map {
    it.toProcessBuilder().directory(dir.toFile()).apply { environment().putAll(env) }
  }
  val processes = ProcessBuilder
    .startPipeline(processBuilders)
    .mapIndexed { index, process ->
      JavaProcess(process, flatCommands[index], outStream, errStream)
    }
  return PipedProcess(this, processes)
}

private fun Command.flatten(): List<Command> = when (this) {
  is Pipe -> commands.flatMap { it.flatten() }
  else -> listOf(this)
}

private fun Command.toProcessBuilder() = when (this) {
  is Shell -> this.toProcessBuilder()
  is Exec -> this.toProcessBuilder()
  is Pipe -> throw IllegalArgumentException("cannot convert $this to a ProcessBuilder")
}

private fun Shell.toProcessBuilder() = ProcessBuilder("/usr/bin/env", "sh", "-c", command)

private fun Exec.toProcessBuilder() = ProcessBuilder(executable, *args.toTypedArray())

private fun Command.run(
  processBuilder: ProcessBuilder,
  dir: Path,
  env: Map<String, String>,
  outStream: Appendable,
  errStream: Appendable,
): Process {
  processBuilder.environment().putAll(env)
  processBuilder.directory(dir.toFile())
  return JavaProcess(
    processBuilder.start(),
    this,
    outStream,
    errStream,
  )
}
