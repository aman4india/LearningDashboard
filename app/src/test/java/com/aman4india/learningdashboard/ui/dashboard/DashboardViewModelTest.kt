package com.aman4india.learningdashboard.ui.dashboard

import com.aman4india.learningdashboard.data.repository.CourseRepositoryImpl
import com.aman4india.learningdashboard.fakes.FakeAuthRepository
import com.aman4india.learningdashboard.fakes.FakeCourseApi
import com.aman4india.learningdashboard.fakes.FakeCourseDao
import com.aman4india.learningdashboard.fakes.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeCourseApi()
    private val dao = FakeCourseDao()
    private val repository = CourseRepositoryImpl(api, dao)

    private fun TestScope.createViewModel(): DashboardViewModel {
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())
        backgroundScope.launch(mainDispatcherRule.dispatcher) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `shows courses when the API succeeds`() = runTest {
        val state = createViewModel().uiState.value

        assertTrue(state is DashboardUiState.Success)
        state as DashboardUiState.Success
        assertEquals(1, state.courses.size)
        assertEquals(null, state.offlineMessage)
    }

    @Test
    fun `shows previously cached courses with offline message when the API fails`() = runTest {
        repository.refreshCourses() // data was loaded once
        api.error = IOException("No internet")

        val state = createViewModel().uiState.value

        assertTrue(state is DashboardUiState.Success)
        state as DashboardUiState.Success
        assertEquals("Python Programming", state.courses.single().title)
        assertEquals("You're offline. Showing saved courses.", state.offlineMessage)
    }

    @Test
    fun `shows error when the API fails and nothing is cached`() = runTest {
        api.error = IOException("No internet")

        val state = createViewModel().uiState.value

        assertTrue(state is DashboardUiState.Error)
    }

    @Test
    fun `shows empty state when the API returns no courses`() = runTest {
        api.courses = emptyList()

        val state = createViewModel().uiState.value

        assertEquals(DashboardUiState.Empty, state)
    }

    @Test
    fun `retry after failure loads courses`() = runTest {
        api.error = IOException("No internet")
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value is DashboardUiState.Error)

        api.error = null
        viewModel.refresh()

        assertTrue(viewModel.uiState.value is DashboardUiState.Success)
        assertEquals(2, api.callCount)
    }
}
