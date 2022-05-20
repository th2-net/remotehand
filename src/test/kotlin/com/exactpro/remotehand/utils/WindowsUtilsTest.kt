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

package com.exactpro.remotehand.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class WindowsUtilsTest {
    @Test
    fun testIsSameHandle() {
        assertTrue(isSameHandle("0x00000000002E095C", "0x002E095C"))
        assertTrue(isSameHandle("0x002E095C", "0x00000000002E095C"))

        assertFalse(isSameHandle("0x00000000005E095C", "0x002E095C"))
        assertFalse(isSameHandle("0x00000000005E095C", "0x00000000002E095C"))
        assertTrue(isSameHandle("0x002E095C", "0x002E095C"))
        assertFalse(isSameHandle("0x002E095C", "0x005E095C"))

        assertFalse(isSameHandle("0x0_NOT_NUMBER", "0x005E095C"))
        assertFalse(isSameHandle("0x005E095C", "0x0NOT_NUMBER"))

        assertTrue(isSameHandle("0xBA21", "0xBA21"))
        assertTrue(isSameHandle("0XBA21", "0xBA21"))

        assertTrue(isSameHandle("str768342", "str768342"))
        assertTrue(isSameHandle("string", "StRiNg"))
        assertFalse(isSameHandle("str76_342", "str768342"))
        assertTrue(isSameHandle("1234567", "1234567"))

        assertFalse(isSameHandle(null, "768342"))
        assertFalse(isSameHandle("768342", null))
        assertFalse(isSameHandle(null, null))
    }
}