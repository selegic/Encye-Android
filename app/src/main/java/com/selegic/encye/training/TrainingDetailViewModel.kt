package com.selegic.encye.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selegic.encye.data.remote.dto.TrainingDto
import com.selegic.encye.data.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainingDetailUiState(
    val isLoading: Boolean = true,
    val training: TrainingDto? = null,
    val errorMessage: String? = null,
    val loadedTrainingId: String? = null
)

@HiltViewModel
class TrainingDetailViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingDetailUiState())
    val uiState: StateFlow<TrainingDetailUiState> = _uiState.asStateFlow()

    fun loadTraining(trainingId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (!forceRefresh &&
                currentState.training != null &&
                currentState.loadedTrainingId == trainingId &&
                currentState.errorMessage == null
            ) {
                return@launch
            }

            val cachedTraining = trainingRepository.getCachedTrainingById(trainingId)
            _uiState.update {
                it.copy(
                    isLoading = cachedTraining == null,
                    training = cachedTraining ?: if (it.loadedTrainingId == trainingId) it.training else null,
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
                val latestCachedTraining = trainingRepository.getCachedTrainingById(trainingId) ?: cachedTraining
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
    }
}
