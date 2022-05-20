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