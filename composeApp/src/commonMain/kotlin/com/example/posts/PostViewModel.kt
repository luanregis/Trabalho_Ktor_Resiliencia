package com.example.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val userId: Int? = null,
    val endOfPaginationReached: Boolean = false
)

class PostViewModel(private val repository: PostRepository = PostRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts(reset: Boolean = false) {
        if (_uiState.value.isLoading) return
        if (reset) {
            _uiState.update { it.copy(page = 1, posts = emptyList(), endOfPaginationReached = false, error = null) }
        } else if (_uiState.value.endOfPaginationReached) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentState = _uiState.value
                val newPosts = repository.getPosts(currentState.page, 10, currentState.userId)
                
                if (newPosts.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, endOfPaginationReached = true) }
                } else {
                    _uiState.update { 
                        it.copy(
                            posts = it.posts + newPosts,
                            page = it.page + 1,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro desconhecido") }
            }
        }
    }

    fun updateUserId(userIdStr: String) {
        val userId = userIdStr.toIntOrNull()
        _uiState.update { it.copy(userId = userId) }
    }
}
