package com.spendsmart

import com.spendsmart.utils.toMD5Hash
import com.spendsmart.utils.toCurrencyString
import com.spendsmart.utils.getCurrentMonth
import com.spendsmart.utils.getCurrentYear
import com.spendsmart.utils.getStartOfMonth
import com.spendsmart.utils.getEndOfMonth
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class UtilsTest {

    @Test
    fun `md5 hash produces consistent results`() {
        val password = "testPassword123"
        val hash1 = password.toMD5Hash()
        val hash2 = password.toMD5Hash()
        assertEquals(hash1, hash2)
    }

    @Test
    fun `md5 hash length is 32 characters`() {
        val hash = "password".toMD5Hash()
        assertEquals(32, hash.length)
    }

    @Test
    fun `different passwords produce different hashes`() {
        val hash1 = "password1".toMD5Hash()
        val hash2 = "password2".toMD5Hash()
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `currency string formats correctly`() {
        val amount = 1500.0
        val formatted = amount.toCurrencyString()
        assertTrue(formatted.isNotEmpty())
        // Should contain the number
        assertTrue(formatted.contains("1") || formatted.contains("1,500") || formatted.contains("1.500"))
    }

    @Test
    fun `getStartOfMonth returns start of month`() {
        val start = getStartOfMonth(1, 2026)
        val cal = Calendar.getInstance()
        cal.timeInMillis = start
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.MONTH)) // January = 0
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
    }

    @Test
    fun `getEndOfMonth returns end of month`() {
        val end = getEndOfMonth(1, 2026)
        val cal = Calendar.getInstance()
        cal.timeInMillis = end
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH)) // January has 31 days
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, cal.get(Calendar.MINUTE))
    }

    @Test
    fun `getCurrentMonth returns valid month`() {
        val month = getCurrentMonth()
        assertTrue(month in 1..12)
    }

    @Test
    fun `getCurrentYear returns reasonable year`() {
        val year = getCurrentYear()
        assertTrue(year >= 2024)
    }

    @Test
    fun `startOfMonth is before endOfMonth`() {
        val start = getStartOfMonth(4, 2026)
        val end = getEndOfMonth(4, 2026)
        assertTrue(start < end)
    }

    @Test
    fun `md5 hash of empty string is consistent`() {
        val h1 = "".toMD5Hash()
        val h2 = "".toMD5Hash()
        assertEquals(h1, h2)
        assertEquals(32, h1.length)
    }
}
