package com.aman4india.learningdashboard.data.remote

import android.content.Context
import com.aman4india.learningdashboard.di.IoDispatcher
import com.aman4india.learningdashboard.domain.repository.InvalidCredentialsException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

private const val MOCK_LATENCY_MS = 800L

/**
 * Simulates a backend by reading `assets/courses.json`. It fails with [IOException] when the
 * device is offline so the offline/cache behaviour can be exercised on a real device.
 */
class MockCourseApi @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CourseApi {
    override suspend fun getCourses(): List<CourseDto> = withContext(ioDispatcher) {
        delay(MOCK_LATENCY_MS)
        if (!networkMonitor.isOnline()) throw IOException("No internet connection")
        val body = context.assets.open("courses.json").bufferedReader().use { it.readText() }
        json.decodeFromString<List<CourseDto>>(body)
    }
}

class MockAuthApi @Inject constructor(
    private val networkMonitor: NetworkMonitor,
) : AuthApi {
    override suspend fun login(email: String, password: String): LoginResponseDto {
        delay(MOCK_LATENCY_MS)
        if (!networkMonitor.isOnline()) throw IOException("No internet connection")
        if (!email.trim().equals(DEMO_EMAIL, ignoreCase = true) || password != DEMO_PASSWORD) {
            throw InvalidCredentialsException()
        }
        return LoginResponseDto(token = "mock-${UUID.randomUUID()}")
    }

    companion object {
        const val DEMO_EMAIL = "aman@gmail.com"
        const val DEMO_PASSWORD = "123456"
    }
}
