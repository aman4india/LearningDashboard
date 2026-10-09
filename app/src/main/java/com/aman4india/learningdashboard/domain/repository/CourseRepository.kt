package com.aman4india.learningdashboard.domain.repository

import com.aman4india.learningdashboard.domain.model.Course
import kotlinx.coroutines.flow.Flow

/**
 * Courses are always read from the local cache (single source of truth);
 * [refreshCourses] syncs the cache with the remote API.
 */
interface CourseRepository {
    fun observeCourses(): Flow<List<Course>>

    fun observeCourse(courseId: Long): Flow<Course?>

    /** Fetches the latest courses from the API and stores them locally. Throws on failure. */
    suspend fun refreshCourses()

    suspend fun markLessonCompleted(lessonId: Long)

    suspend fun clearCache()
}
