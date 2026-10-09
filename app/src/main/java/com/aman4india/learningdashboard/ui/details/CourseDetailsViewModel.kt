package com.aman4india.learningdashboard.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aman4india.learningdashboard.domain.model.Course
import com.aman4india.learningdashboard.domain.repository.CourseRepository
import com.aman4india.learningdashboard.ui.navigation.CourseDetailsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CourseDetailsUiState {
    data object Loading : CourseDetailsUiState
    data object NotFound : CourseDetailsUiState
    data class Success(val course: Course) : CourseDetailsUiState
}

@HiltViewModel
class CourseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val courseId: Long = savedStateHandle.toRoute<CourseDetailsDestination>().courseId

    val uiState: StateFlow<CourseDetailsUiState> =
        courseRepository.observeCourse(courseId)
            .map { course ->
                if (course == null) CourseDetailsUiState.NotFound else CourseDetailsUiState.Success(course)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseDetailsUiState.Loading)

    fun markLessonCompleted(lessonId: Long) {
        viewModelScope.launch { courseRepository.markLessonCompleted(lessonId) }
    }
}
