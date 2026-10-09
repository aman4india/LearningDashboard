package com.aman4india.learningdashboard.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aman4india.learningdashboard.domain.model.Course
import com.aman4india.learningdashboard.domain.model.Lesson
import com.aman4india.learningdashboard.ui.theme.LearningDashboardTheme

@Composable
fun CourseDetailsRoute(
    onBack: () -> Unit,
    viewModel: CourseDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CourseDetailsScreen(
        state = state,
        onBack = onBack,
        onMarkCompleted = viewModel::markLessonCompleted,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsScreen(
    state: CourseDetailsUiState,
    onBack: () -> Unit,
    onMarkCompleted: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course details") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                CourseDetailsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                CourseDetailsUiState.NotFound -> Text(
                    "This course is no longer available.",
                    modifier = Modifier.align(Alignment.Center),
                )
                is CourseDetailsUiState.Success -> CourseDetailsContent(state.course, onMarkCompleted)
            }
        }
    }
}

@Composable
private fun CourseDetailsContent(course: Course, onMarkCompleted: (Long) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "header") {
            Column(Modifier.padding(bottom = 16.dp)) {
                Text(course.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "by ${course.instructor}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Progress: ${course.progressPercent}% " +
                        "(${course.completedLessons}/${course.totalLessons} lessons)",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { course.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        itemsIndexed(course.lessons, key = { _, lesson -> lesson.id }) { index, lesson ->
            LessonRow(index + 1, lesson, onMarkCompleted)
            HorizontalDivider()
        }
    }
}

@Composable
private fun LessonRow(number: Int, lesson: Lesson, onMarkCompleted: (Long) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text("$number. ${lesson.title}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (lesson.isCompleted) {
            Text(
                "✓ Completed",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "○ Pending",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
                TextButton(onClick = { onMarkCompleted(lesson.id) }) { Text("Mark complete") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailsPreview() {
    val lessons = listOf(
        Lesson(1, "Introduction", true),
        Lesson(2, "Variables & Data Types", true),
        Lesson(3, "Functions", false),
        Lesson(4, "OOP", false),
    )
    LearningDashboardTheme {
        CourseDetailsScreen(
            state = CourseDetailsUiState.Success(Course(1, "Python Programming", "John Smith", lessons)),
            onBack = {},
            onMarkCompleted = {},
        )
    }
}
