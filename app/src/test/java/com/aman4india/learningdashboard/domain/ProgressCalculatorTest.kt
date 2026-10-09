package com.aman4india.learningdashboard.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {

    @Test
    fun `calculates rounded percentage and handles edge cases`() {
        assertEquals(65, ProgressCalculator.percent(13, 20))
        assertEquals(38, ProgressCalculator.percent(6, 16)) // 37.5 rounds half-up
        assertEquals(0, ProgressCalculator.percent(0, 0)) // no lessons
        assertEquals(100, ProgressCalculator.percent(5, 5))
        assertEquals(100, ProgressCalculator.percent(7, 5)) // clamped
    }
}
