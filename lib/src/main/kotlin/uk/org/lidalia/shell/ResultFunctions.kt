package uk.org.lidalia.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok

fun <T : Any> T.ok(): Ok<T> = Ok(this)

fun <T : Any> T.err(): Err<T> = Err(this)
