package com.example.flashappcs4520.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashappcs4520.common.Comment
import com.example.flashappcs4520.data.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Loads the current user's own comments for the "My Comments" page. Each comment links back to
 * its article + comment via the same deep link the notifications use.
 */
class MyCommentsViewModel(
    private val repository: CommentRepository = CommentRepository(),
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _comments.value = repository.fetchMyComments()
            } catch (e: Exception) {
                Log.e("MyCommentsViewModel", "Failed to load my comments", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}