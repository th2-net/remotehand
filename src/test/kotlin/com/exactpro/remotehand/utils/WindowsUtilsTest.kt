package com.exactpro.remotehand.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class WindowsUtilsTest {
    @Test
    fun testIsSameHandler() {
        assertTrue(isSameHandler("0x00000000002E095C", "0x002E095C"))
        assertTrue(isSameHandler("0x002E095C", "0x00000000002E095C"))

        assertFalse(isSameHandler("0x00000000005E095C", "0x002E095C"))
        assertFalse(isSameHandler("0x00000000005E095C", "0x00000000002E095C"))
        assertTrue(isSameHandler("0x002E095C", "0x002E095C"))
        assertFalse(isSameHandler("0x002E095C", "0x005E095C"))

        assertTrue(isSameHandler("0xBA21", "0xBA21"))
        assertTrue(isSameHandler("0XBA21", "0xBA21"))

        assertTrue(isSameHandler("str768342", "str768342"))
        assertTrue(isSameHandler("string", "StRiNg"))
        assertFalse(isSameHandler("str76_342", "str768342"))
        assertTrue(isSameHandler("1234567", "1234567"))

        assertFalse(isSameHandler(null, "768342"))
        assertFalse(isSameHandler("768342", null))
        assertFalse(isSameHandler(null, null))
    }
}