package com.aman4india.learningdashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman4india.learningdashboard.domain.model.Course
import com.aman4india.learningdashboard.domain.repository.AuthRepository
import com.aman4india.learningdashboard.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Error(val message: String) : DashboardUiState

    /** [offlineMessage] is set when the list comes from cache because the refresh failed. */
    data class Success(
        val courses: List<Course>,
        val isRefreshing: Boolean = false,
        val offlineMessage: String? = null,
    ) : DashboardUiState
}

private sealed interface RefreshState {
    data object Loading : RefreshState
    data object Done : RefreshState
    data class Failed(val error: Exception) : RefreshState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.Loading)

    val uiState: StateFlow<DashboardUiState> =
        combine(courseRepository.observeCourses(), refreshState) { courses, refresh ->
            when {
                courses.isNotEmpty() -> DashboardUiState.Success(
                    courses = courses,
                    isRefreshing = refresh is RefreshState.Loading,
                    offlineMessage = (refresh as? RefreshState.Failed)?.let { it.error.toOfflineMessage() },
                )
                refresh is RefreshState.Loading -> DashboardUiState.Loading
                refresh is RefreshState.Failed -> DashboardUiState.Error(refresh.error.toErrorMessage())
                else -> DashboardUiState.Empty
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        refreshState.value = RefreshState.Loading
        viewModelScope.launch {
            refreshState.value = try {
                courseRepository.refreshCourses()
                RefreshState.Done
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RefreshState.Failed(e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            courseRepository.clearCache()
            authRepository.logout()
        }
    }

    private fun Exception.toErrorMessage(): String = when (this) {
        is IOException -> "Couldn't load courses. Check your internet connection and try again."
        else -> "Something went wrong while loading courses."
    }

    private fun Exception.toOfflineMessage(): String = when (this) {
        is IOException -> "You're offline. Showing saved courses."
        else -> "Couldn't refresh. Showing saved courses."
    }
}
