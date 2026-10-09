package com.aman4india.learningdashboard.fakes

import com.aman4india.learningdashboard.data.local.CourseDao
import com.aman4india.learningdashboard.data.local.CourseEntity
import com.aman4india.learningdashboard.data.local.CourseWithLessons
import com.aman4india.learningdashboard.data.local.LessonEntity
import com.aman4india.learningdashboard.data.remote.CourseApi
import com.aman4india.learningdashboard.data.remote.CourseDto
import com.aman4india.learningdashboard.data.remote.LessonDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory replacement for the Room DAO that preserves its observable semantics. */
class FakeCourseDao : CourseDao() {
    private val courses = MutableStateFlow<List<CourseEntity>>(emptyList())
    private val lessons = MutableStateFlow<List<LessonEntity>>(emptyList())

    private val joined: Flow<List<CourseWithLessons>> =
        kotlinx.coroutines.flow.combine(courses, lessons) { c, l ->
            c.sortedBy { it.position }.map { course ->
                CourseWithLessons(course, l.filter { it.courseId == course.id })
            }
        }

    override fun observeCourses(): Flow<List<CourseWithLessons>> = joined

    override fun observeCourse(courseId: Long): Flow<CourseWithLessons?> =
        joined.map { list -> list.firstOrNull { it.course.id == courseId } }

    override suspend fun getCompletedLessonIds(): List<Long> =
        lessons.value.filter { it.isCompleted }.map { it.id }

    override suspend fun markLessonCompleted(lessonId: Long) =
        lessons.update { list -> list.map { if (it.id == lessonId) it.copy(isCompleted = true) else it } }

    override suspend fun upsertCourses(courses: List<CourseEntity>) =
        this.courses.update { existing -> (existing.associateBy { it.id } + courses.associateBy { it.id }).values.toList() }

    override suspend fun upsertLessons(lessons: List<LessonEntity>) =
        this.lessons.update { existing -> (existing.associateBy { it.id } + lessons.associateBy { it.id }).values.toList() }

    override suspend fun deleteCoursesNotIn(keepIds: List<Long>) {
        courses.update { list -> list.filter { it.id in keepIds } }
        lessons.update { list -> list.filter { it.courseId in keepIds } }
    }

    override suspend fun deleteLessonsNotIn(keepIds: List<Long>) =
        lessons.update { list -> list.filter { it.id in keepIds } }

    override suspend fun clearAll() {
        courses.value = emptyList()
        lessons.value = emptyList()
    }
}

class FakeCourseApi(var courses: List<CourseDto> = sampleCourses()) : CourseApi {
    var error: Exception? = null
    var callCount = 0
        private set

    override suspend fun getCourses(): List<CourseDto> {
        callCount++
        error?.let { throw it }
        return courses
    }
}

/** One course with 4 lessons, 2 completed on the server (50%). */
fun sampleCourses(): List<CourseDto> = listOf(
    CourseDto(
        id = 1,
        title = "Python Programming",
        instructor = "John Smith",
        lessons = listOf(
            LessonDto(101, "Introduction", completed = true),
            LessonDto(102, "Variables & Data Types", completed = true),
            LessonDto(103, "Functions", completed = false),
            LessonDto(104, "OOP", completed = false),
        ),
    ),
)
