package com.aman4india.learningdashboard.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: Long,
    val title: String,
    val instructor: String,
    val lessons: List<LessonDto> = emptyList(),
)

@Serializable
data class LessonDto(
    val id: Long,
    val title: String,
    val completed: Boolean = false,
)

@Serializable
data class LoginResponseDto(
    val token: String,
)
