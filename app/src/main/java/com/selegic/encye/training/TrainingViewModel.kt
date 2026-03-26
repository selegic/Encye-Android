package com.selegic.encye.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selegic.encye.data.remote.dto.TrainingDto
import com.selegic.encye.data.remote.dto.UserEnrollmentDto
import com.selegic.encye.data.remote.dto.UserProgressDto
import com.selegic.encye.data.repository.EnrollProgressRepository
import com.selegic.encye.data.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainingUiState(
    val isLoading: Boolean = true,
    val trainings: List<TrainingDto> = emptyList(),
    val enrollments: List<UserProgressDto> = emptyList(),
    val isEnrollmentLoading: Boolean = false,
    val isProgressUpdateLoading: Boolean = false,
    val latestEnrollment: UserEnrollmentDto? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val enrollProgressRepository: EnrollProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    init {
        loadTrainingsInternal(forceRefresh = false)
    }

    fun loadTrainings() {
        loadTrainingsInternal(forceRefresh = true)
    }

    fun getEnrollments() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isEnrollmentLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                enrollProgressRepository.getEnrollments()
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isEnrollmentLoading = false,
                        enrollments = response.data.orEmpty(),
                        errorMessage = if (response.success) null else response.msg.ifBlank {
                            "Unable to load enrollments"
                        }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isEnrollmentLoading = false,
                        errorMessage = error.message ?: "Unable to load enrollments"
                    )
                }
            }
        }
    }

    private fun loadTrainingsInternal(forceRefresh: Boolean) {
        viewModelScope.launch {
            val cachedTrainings = trainingRepository.getCachedTrainings()
            _uiState.update {
                it.copy(
                    isLoading = cachedTrainings.isEmpty(),
                    trainings = cachedTrainings,
                    errorMessage = null
                )
            }

            runCatching {
                trainingRepository.refreshTrainingsIfStale(
                    ttlMillis = TRAINING_CACHE_TTL_MILLIS,
                    force = forceRefresh
                )
            }.onSuccess { response ->
                val latestTrainings = trainingRepository.getCachedTrainings()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trainings = latestTrainings,
                        errorMessage = if (response.success) {
                            null
                        } else {
                            response.msg.ifBlank { "Unable to refresh trainings" }
                        }
                    )
                }
            }.onFailure { error ->
                val latestTrainings = trainingRepository.getCachedTrainings()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trainings = latestTrainings,
                        errorMessage = error.message ?: "Unable to load trainings"
                    )
                }
            }
        }
    }
}
