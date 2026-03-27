package com.selegic.encye.training

import android.util.Log
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import javax.inject.Inject

data class TrainingDetailUiState(
    val isLoading: Boolean = true,
    val isEnrollmentLoading: Boolean = false,
    val isEnrolled: Boolean = false,
    val training: TrainingDto? = null,
    val latestEnrollment: UserEnrollmentDto? = null,
    val errorMessage: String? = null,
    val loadedTrainingId: String? = null
)

private const val TAG = "TrainingDetailViewModel"


@HiltViewModel
class TrainingDetailViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val enrollProgressRepository: EnrollProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingDetailUiState())
    val uiState: StateFlow<TrainingDetailUiState> = _uiState.asStateFlow()

    suspend fun fetchEnrollments(trainingId: String) {
        enrollProgressRepository.getAllEnrollments().collect { enrollments ->
            _uiState.update {
                it.copy(
                    isEnrolled = enrollments.any {
                        it.matchesTrainingId(trainingId)
                    }
                )
            }
        }
    }

    suspend fun loadTraining(trainingId: String, forceRefresh: Boolean = false) {
        val currentState = _uiState.value
        if (!forceRefresh &&
            currentState.training != null &&
            currentState.loadedTrainingId == trainingId &&
            currentState.errorMessage == null
        ) {
            return
        }

        val cachedTraining = trainingRepository.getCachedTrainingById(trainingId)
        _uiState.update {
            it.copy(
                isLoading = cachedTraining == null,
                training = cachedTraining
                    ?: if (it.loadedTrainingId == trainingId) it.training else null,
                errorMessage = null,
                loadedTrainingId = trainingId
            )
        }

        runCatching {
            // Always refresh training detail by ID because the list cache can contain
            // partial module payloads that omit rich fields like quiz content.
            trainingRepository.refreshTrainingById(trainingId)
        }.onSuccess { response ->
            val latestCachedTraining = trainingRepository.getCachedTrainingById(trainingId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    training = latestCachedTraining ?: response.data ?: cachedTraining,
                    errorMessage = if (response.success) null else response.msg,
                    loadedTrainingId = trainingId
                )
            }
        }.onFailure { error ->
            val latestCachedTraining =
                trainingRepository.getCachedTrainingById(trainingId) ?: cachedTraining
            _uiState.update {
                it.copy(
                    isLoading = false,
                    training = latestCachedTraining,
                    errorMessage = error.message ?: "Unable to load training",
                    loadedTrainingId = trainingId
                )
            }
        }
    }

    fun enrollInTraining(trainingId: String) {
        viewModelScope.launch {
            if (_uiState.value.isEnrollmentLoading || _uiState.value.isEnrolled) return@launch

            _uiState.update {
                it.copy(
                    isEnrollmentLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                enrollProgressRepository.enrollUser(trainingId)
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isEnrollmentLoading = false,
                        latestEnrollment = response.data,
                        errorMessage = if (response.success) null else response.msg.ifBlank {
                            "Unable to enroll in training"
                        }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isEnrollmentLoading = false,
                        errorMessage = error.message ?: "Unable to enroll in training"
                    )
                }
            }
        }
    }
}

private fun UserProgressDto.matchesTrainingId(trainingId: String): Boolean {
    return trainingId == this.trainingId.id
}

private fun String.trainingIdValueMatches(trainingIdValue: JsonElement?): Boolean {
    val expectedId = this
    val rawContent = (trainingIdValue as? JsonPrimitive)?.contentOrNull
    if (rawContent == expectedId) return true

    val objectId = (trainingIdValue as? JsonObject)
        ?.get("_id")
        ?.let { it as? JsonPrimitive }
        ?.contentOrNull

    return objectId == expectedId
}
