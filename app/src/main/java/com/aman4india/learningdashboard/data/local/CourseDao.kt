package com.aman4india.learningdashboard.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseDao {

    @Transaction
    @Query("SELECT * FROM courses ORDER BY position")
    abstract fun observeCourses(): Flow<List<CourseWithLessons>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :courseId")
    abstract fun observeCourse(courseId: Long): Flow<CourseWithLessons?>

    @Query("SELECT id FROM lessons WHERE isCompleted = 1")
    abstract suspend fun getCompletedLessonIds(): List<Long>

    @Query("UPDATE lessons SET isCompleted = 1 WHERE id = :lessonId")
    abstract suspend fun markLessonCompleted(lessonId: Long)

    @Upsert
    protected abstract suspend fun upsertCourses(courses: List<CourseEntity>)

    @Upsert
    protected abstract suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM courses WHERE id NOT IN (:keepIds)")
    protected abstract suspend fun deleteCoursesNotIn(keepIds: List<Long>)

    @Query("DELETE FROM lessons WHERE id NOT IN (:keepIds)")
    protected abstract suspend fun deleteLessonsNotIn(keepIds: List<Long>)

    @Query("DELETE FROM courses")
    abstract suspend fun clearAll()

    /** Atomically replaces the cached catalogue with [courses]/[lessons]. */
    @Transaction
    open suspend fun replaceAll(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        deleteCoursesNotIn(courses.map { it.id })
        deleteLessonsNotIn(lessons.map { it.id })
        upsertCourses(courses)
        upsertLessons(lessons)
    }
}
