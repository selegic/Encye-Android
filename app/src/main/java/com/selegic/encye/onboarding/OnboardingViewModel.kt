package com.selegic.encye.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selegic.encye.data.repository.UserRepository
import com.selegic.encye.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingUiState {
    data object Idle : OnboardingUiState()
    data object Loading : OnboardingUiState()
    data class Success(val isNewUser: Boolean) : OnboardingUiState()
    data class Error(val message: String) : OnboardingUiState()
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onGoogleAuthCodeReceived(code: String) {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading
            try {
                val response = userRepository.googleAuthCallback(code)
                val data = response.data
                if (data != null) {
                    // Save the token locally
                    sessionManager.saveAuthToken(data.token)
                    _uiState.value = OnboardingUiState.Success(isNewUser = data.new)
                } else {
                    _uiState.value = OnboardingUiState.Error("Invalid response from server")
                }
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
