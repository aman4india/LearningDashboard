package com.aman4india.learningdashboard.data.remote

/** Remote course endpoints. Swap [MockCourseApi] for a Retrofit/Ktor implementation in production. */
interface CourseApi {
    suspend fun getCourses(): List<CourseDto>
}

interface AuthApi {
    suspend fun login(email: String, password: String): LoginResponseDto
}
