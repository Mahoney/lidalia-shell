package uk.org.lidalia.shell

fun String.containsAny(chars: Collection<Char>): Boolean = any { chars.contains(it) }

private inline fun String.any(predicate: (Char) -> Boolean) = toCharArray().any(predicate)
