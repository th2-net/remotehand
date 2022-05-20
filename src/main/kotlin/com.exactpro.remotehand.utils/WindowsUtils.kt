/*
 * Copyright 2022-2022 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("WindowsUtils")
package com.exactpro.remotehand.utils

// For the same window we can get from RemoteWebDriver
// 64 bit HEX string or 32 bit HEX string
// (e.g. "0x002E095C" is the same handle as "0x00000000002E095C"),
// so we need check for equality considering this case.
// Any other string are also a valid window handle
fun isSameHandle(h1: String?, h2: String?) = when {
    h1 == null || h2 == null -> false  // 'null' is not valid handle, so two 'null' are not 'same handles'
    h1.length == h2.length -> h1.equals(h2, true)
    h1.startsWith("0x", true) && h2.startsWith("0x", true) ->
        try {
            h1.substring(2).toLong(16) == h2.substring(2).toLong(16)
        } catch (e: NumberFormatException) {
            false
        }
    else -> false
}