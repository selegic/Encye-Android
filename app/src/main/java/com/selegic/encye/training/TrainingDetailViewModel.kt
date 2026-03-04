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
        val currentState = _uiState.value
        if (!forceRefresh &&
            currentState.training != null &&
            currentState.loadedTrainingId == trainingId &&
            currentState.errorMessage == null
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                trainingRepository.getTrainingById(trainingId)
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        training = response.data,
                        errorMessage = if (response.success) null else response.msg,
                        loadedTrainingId = trainingId
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        training = null,
                        errorMessage = error.message ?: "Unable to load training",
                        loadedTrainingId = trainingId
                    )
                }
            }
        }
    }
}
