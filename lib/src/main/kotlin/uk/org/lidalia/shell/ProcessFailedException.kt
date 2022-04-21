package uk.org.lidalia.shell

class ProcessFailedException(
  val failure: Failed
) : RuntimeException(failure.toString())
