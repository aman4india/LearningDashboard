package com.aman4india.learningdashboard.ui.login

import com.aman4india.learningdashboard.domain.repository.InvalidCredentialsException
import com.aman4india.learningdashboard.fakes.FakeAuthRepository
import com.aman4india.learningdashboard.fakes.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val viewModel = LoginViewModel(authRepository)

    @Test
    fun `invalid input shows field errors and does not call the API`() {
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("123")

        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("Enter a valid email address", state.emailError)
        assertNotNull(state.passwordError)
        assertEquals(0, authRepository.loginCalls)
    }

    @Test
    fun `valid credentials log the user in`() {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.login()

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedIn)
        assertFalse(state.isLoading)
    }

    @Test
    fun `rejected credentials show an error`() {
        authRepository.loginError = InvalidCredentialsException()
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("wrongpass")

        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("Invalid email or password", state.errorMessage)
        assertFalse(state.isLoggedIn)
        assertFalse(state.isLoading)
    }
}
