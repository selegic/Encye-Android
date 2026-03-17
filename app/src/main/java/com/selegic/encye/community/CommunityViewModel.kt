package com.selegic.encye.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selegic.encye.data.remote.dto.CommunityDto
import com.selegic.encye.data.repository.CommunityRepository
import com.selegic.encye.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityUiState(
    val isLoading: Boolean = true,
    val communities: List<CommunityDto> = emptyList(),
    val currentUserId: String? = null,
    val updatingCommunityIds: Set<String> = emptySet(),
    val errorMessage: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        fetchCommunities()
    }

    fun fetchCommunities() {
        fetchCommunities(showLoading = true)
    }

    private fun fetchCommunities(showLoading: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = if (showLoading) true else it.isLoading,
                    errorMessage = null
                )
            }

            runCatching {
                communityRepository.getAllCommunities()
            }.onSuccess { communities ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        communities = communities,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        communities = emptyList(),
                        errorMessage = error.message ?: "Unable to load communities"
                    )
                }
            }
        }
    }

    fun toggleCommunityMembership(community: CommunityDto) {
        if (community.isAdmin || community.isModerator) {
            return
        }

        viewModelScope.launch {
            val currentUserId = _uiState.value.currentUserId ?: sessionManager.getCurrentUserId()
            if (currentUserId.isNullOrBlank()) {
                _uiState.update {
                    it.copy(errorMessage = "Sign in to join or leave communities")
                }
                return@launch
            }

            val currentCommunity = _uiState.value.communities.firstOrNull {
                it.mongoId == community.mongoId
            } ?: community

            if (currentCommunity.mongoId in _uiState.value.updatingCommunityIds) {
                return@launch
            }

            val isMember = currentCommunity.member.contains(currentUserId)
            val optimisticCommunity = currentCommunity.copy(
                member = if (isMember) {
                    currentCommunity.member.filterNot { it == currentUserId }
                } else {
                    currentCommunity.member + currentUserId
                }
            )

            _uiState.update { state ->
                state.copy(
                    communities = state.communities.map {
                        if (it.mongoId == currentCommunity.mongoId) optimisticCommunity else it
                    },
                    updatingCommunityIds = state.updatingCommunityIds + currentCommunity.mongoId,
                    errorMessage = null,
                    currentUserId = currentUserId
                )
            }

            runCatching {
                communityRepository.updateCommunityMembership(
                    communityId = currentCommunity.mongoId,
                    join = !isMember,
                    leave = isMember
                )
            }.onSuccess { updatedCommunity ->
                _uiState.update { state ->
                    state.copy(
                        communities = state.communities.map {
                            if (it.mongoId == currentCommunity.mongoId) updatedCommunity else it
                        },
                        updatingCommunityIds = state.updatingCommunityIds - currentCommunity.mongoId
                    )
                }
                fetchCommunities(showLoading = false)
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        communities = state.communities.map {
                            if (it.mongoId == currentCommunity.mongoId) currentCommunity else it
                        },
                        updatingCommunityIds = state.updatingCommunityIds - currentCommunity.mongoId,
                        errorMessage = error.message ?: "Unable to update community membership"
                    )
                }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val currentUserId = sessionManager.getCurrentUserId()
            _uiState.update {
                it.copy(currentUserId = currentUserId)
            }
        }
    }
}
