package com.aman4india.learningdashboard.data.repository

import com.aman4india.learningdashboard.fakes.FakeCourseApi
import com.aman4india.learningdashboard.fakes.FakeCourseDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class CourseRepositoryImplTest {

    private val api = FakeCourseApi()
    private val dao = FakeCourseDao()
    private val repository = CourseRepositoryImpl(api, dao)

    @Test
    fun `marking a lesson completed updates lesson status and course progress`() = runTest {
        repository.refreshCourses()
        assertEquals(50, repository.observeCourse(1).first()!!.progressPercent)

        repository.markLessonCompleted(103)

        val course = repository.observeCourse(1).first()!!
        assertTrue(course.lessons.first { it.id == 103L }.isCompleted)
        assertEquals(3, course.completedLessons)
        assertEquals(75, course.progressPercent)
    }

    @Test
    fun `refresh does not overwrite lessons completed locally`() = runTest {
        repository.refreshCourses()
        repository.markLessonCompleted(104)

        // Server still reports lesson 104 as pending.
        repository.refreshCourses()

        val course = repository.observeCourse(1).first()!!
        assertTrue(course.lessons.first { it.id == 104L }.isCompleted)
        assertEquals(75, course.progressPercent)
    }

    @Test
    fun `cached courses remain available when the network fails`() = runTest {
        repository.refreshCourses()

        api.error = IOException("offline")
        try {
            repository.refreshCourses()
            fail("Expected refresh to fail while offline")
        } catch (expected: IOException) {
            // expected
        }

        val cached = repository.observeCourses().first()
        assertEquals(listOf("Python Programming"), cached.map { it.title })
        assertEquals(4, cached.single().totalLessons)
    }
}
