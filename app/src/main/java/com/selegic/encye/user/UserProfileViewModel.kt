package com.selegic.encye.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selegic.encye.data.remote.dto.UserProfileDataDto
import com.selegic.encye.data.repository.UserRepository
import com.selegic.encye.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UserProfileUiState {
    data object Loading : UserProfileUiState
    data class Success(val profile: UserProfileDataDto) : UserProfileUiState
    data class Error(val message: String) : UserProfileUiState
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()
    private var loadedUserId: String? = null

    fun loadIfNeeded(userId: String? = null) {
        viewModelScope.launch {
            val resolvedUserId = userId ?: sessionManager.getCurrentUserId()
            if (resolvedUserId == null) {
                _uiState.value = UserProfileUiState.Error("No signed-in user id found in the stored session token.")
                return@launch
            }
            if (loadedUserId == resolvedUserId && _uiState.value is UserProfileUiState.Success) {
                return@launch
            }
            fetchProfile(resolvedUserId)
        }
    }

    fun refresh(userId: String? = null) {
        viewModelScope.launch {
            val resolvedUserId = userId ?: sessionManager.getCurrentUserId()
            if (resolvedUserId == null) {
                _uiState.value = UserProfileUiState.Error("No signed-in user id found in the stored session token.")
                return@launch
            }
            fetchProfile(resolvedUserId)
        }
    }

    private suspend fun fetchProfile(resolvedUserId: String) {
        _uiState.value = UserProfileUiState.Loading
        runCatching {
            userRepository.getProfileById(resolvedUserId).data
        }.onSuccess { profile ->
            loadedUserId = resolvedUserId
            _uiState.value = UserProfileUiState.Success(profile)
        }.onFailure { error ->
            _uiState.value = UserProfileUiState.Error(
                error.localizedMessage ?: "Unable to load profile."
            )
        }
    }
}
