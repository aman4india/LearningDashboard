package com.aman4india.learningdashboard.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination

@Serializable
data object DashboardDestination

@Serializable
data class CourseDetailsDestination(val courseId: Long)
