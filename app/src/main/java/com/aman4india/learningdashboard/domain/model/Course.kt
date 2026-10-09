package com.aman4india.learningdashboard.domain.model

import com.aman4india.learningdashboard.domain.ProgressCalculator

data class Lesson(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
)

data class Course(
    val id: Long,
    val title: String,
    val instructor: String,
    val lessons: List<Lesson>,
) {
    val totalLessons: Int get() = lessons.size
    val completedLessons: Int get() = lessons.count { it.isCompleted }
    val progressPercent: Int get() = ProgressCalculator.percent(completedLessons, totalLessons)
}
