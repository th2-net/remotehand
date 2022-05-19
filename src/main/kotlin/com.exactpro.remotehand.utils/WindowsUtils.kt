@file:JvmName("WindowsUtils")
package com.exactpro.remotehand.utils

// For the same window we can get from RemoteWebDriver
// 64 bit HEX string or 32 bit HEX string
// (e.g. "0x002E095C" is equals to "0x00000000002E095C"),
// so we need check for equality considering this case.
// Also, any other string also a valid window handler
fun isSameHandler(h1: String?, h2: String?) = when {
    h1 == null || h2 == null -> false  // 'null' is not valid handler, so 'null' isn't same handler as another 'null'
    h1.length == h2.length -> h1.equals(h2, true)
    h1.startsWith("0x", true) && h2.startsWith("0x", true) ->
        try {
            h1.substring(2).toLong(16) == h2.substring(2).toLong(16)
        } catch (e: NumberFormatException) {
            false
        }
    else -> false
}