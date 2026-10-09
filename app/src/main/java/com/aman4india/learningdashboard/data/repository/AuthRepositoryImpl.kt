package com.aman4india.learningdashboard.data.repository

import com.aman4india.learningdashboard.data.remote.AuthApi
import com.aman4india.learningdashboard.data.session.SessionStore
import com.aman4india.learningdashboard.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> =
        sessionStore.token.map { !it.isNullOrEmpty() }.distinctUntilChanged()

    override suspend fun login(email: String, password: String) {
        val response = authApi.login(email.trim(), password)
        sessionStore.saveToken(response.token)
    }

    override suspend fun logout() {
        sessionStore.clear()
    }
}
