package com.selegic.encye.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selegic.encye.data.remote.dto.CommunityDto
import com.selegic.encye.data.repository.CommunityRepository
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
    val errorMessage: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        fetchCommunities()
    }

    fun fetchCommunities() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
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
}
