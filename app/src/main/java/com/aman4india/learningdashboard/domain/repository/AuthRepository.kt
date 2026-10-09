package com.aman4india.learningdashboard.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>

    /** Throws [InvalidCredentialsException] or an IO exception on failure. */
    suspend fun login(email: String, password: String)

    suspend fun logout()
}

class InvalidCredentialsException : Exception("Invalid email or password")
