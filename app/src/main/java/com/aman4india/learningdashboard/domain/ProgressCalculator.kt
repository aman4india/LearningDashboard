package com.aman4india.learningdashboard.domain

object ProgressCalculator {
    fun percent(completed: Int, total: Int): Int {
        if (total <= 0) return 0
        val safeCompleted = completed.coerceIn(0, total)
        return (safeCompleted * 100 + total / 2) / total
    }
}
