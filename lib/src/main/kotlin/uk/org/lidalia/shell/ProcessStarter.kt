package uk.org.lidalia.shell

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrThrow
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

interface ProcessStarter {
  fun run(
    command: CommandInContext,
    outStream: Appendable = System.out,
    errStream: Appendable = System.err,
  ): Process

  fun CommandInContext.execute(
    outStream: Appendable = System.out,
    errStream: Appendable = System.err,
  ): Result<Succeeded, Failed> =
    run(
      this,
      outStream,
      errStream
    )
      .await()

  operator fun CommandInContext.invoke(
    outStream: Appendable = Discard,
    errStream: Appendable = System.err,
  ): String =
    execute(
      outStream,
      errStream,
    )
      .getOrThrow { f -> ProcessFailedException(f) }
      .stdout.trimEnd()

  fun Command.execute(
    dir: Path = Paths.get("."),
    env: Map<String, String> = emptyMap(),
    outStream: Appendable = System.out,
    errStream: Appendable = System.err,
  ): Result<Succeeded, Failed> =
    withContext(dir, env)
      .execute(
        outStream,
        errStream
      )

  operator fun Command.invoke(
    dir: Path = Paths.get("."),
    env: Map<String, String> = emptyMap(),
    outStream: Appendable = Discard,
    errStream: Appendable = System.err,
  ): String =
    withContext(dir, env)
      .invoke(
        outStream,
        errStream,
      )

  operator fun File.invoke(
    vararg args: String,
    dir: Path = Paths.get("."),
    env: Map<String, String> = emptyMap(),
  ): String =
    Exec(this.absolutePath, args.toList())
      .invoke(dir, env)

  operator fun String.invoke(
    dir: Path = Paths.get("."),
    env: Map<String, String> = emptyMap(),
  ): String = Shell(this).invoke(dir, env)

  fun String.execute(
    dir: Path = Paths.get("."),
    env: Map<String, String> = emptyMap(),
  ): Result<Succeeded, Failed> = Shell(this).execute(dir, env)
}
