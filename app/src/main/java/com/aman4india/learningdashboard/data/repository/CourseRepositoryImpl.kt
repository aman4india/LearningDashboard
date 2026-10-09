package com.aman4india.learningdashboard.data.repository

import com.aman4india.learningdashboard.data.local.CourseDao
import com.aman4india.learningdashboard.data.local.CourseEntity
import com.aman4india.learningdashboard.data.local.CourseWithLessons
import com.aman4india.learningdashboard.data.local.LessonEntity
import com.aman4india.learningdashboard.data.remote.CourseApi
import com.aman4india.learningdashboard.data.remote.CourseDto
import com.aman4india.learningdashboard.domain.model.Course
import com.aman4india.learningdashboard.domain.model.Lesson
import com.aman4india.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first repository: the UI only ever observes Room; the network is used to refresh it.
 */
class CourseRepositoryImpl @Inject constructor(
    private val api: CourseApi,
    private val dao: CourseDao,
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        dao.observeCourses().map { list -> list.map { it.toDomain() } }

    override fun observeCourse(courseId: Long): Flow<Course?> =
        dao.observeCourse(courseId).map { it?.toDomain() }

    override suspend fun refreshCourses() {
        val remote = api.getCourses()
        val locallyCompleted = dao.getCompletedLessonIds().toSet()

        val courses = remote.mapIndexed { index, dto -> dto.toEntity(position = index) }
        val lessons = remote.flatMap { course ->
            course.lessons.mapIndexed { index, lesson ->
                LessonEntity(
                    id = lesson.id,
                    courseId = course.id,
                    title = lesson.title,
                    position = index,
                    isCompleted = lesson.completed || lesson.id in locallyCompleted,
                )
            }
        }
        dao.replaceAll(courses, lessons)
    }

    override suspend fun markLessonCompleted(lessonId: Long) {
        dao.markLessonCompleted(lessonId)
    }

    override suspend fun clearCache() {
        dao.clearAll()
    }
}

private fun CourseDto.toEntity(position: Int) = CourseEntity(
    id = id,
    title = title,
    instructor = instructor,
    position = position,
)

private fun CourseWithLessons.toDomain() = Course(
    id = course.id,
    title = course.title,
    instructor = course.instructor,
    lessons = lessons.sortedBy { it.position }.map {
        Lesson(id = it.id, title = it.title, isCompleted = it.isCompleted)
    },
)
