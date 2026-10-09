package com.aman4india.learningdashboard.fakes

import com.aman4india.learningdashboard.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthRepository : AuthRepository {
    private val loggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = loggedIn

    var loginError: Exception? = null
    var loginCalls = 0
        private set

    override suspend fun login(email: String, password: String) {
        loginCalls++
        loginError?.let { throw it }
        loggedIn.value = true
    }

    override suspend fun logout() {
        loggedIn.value = false
    }
}
