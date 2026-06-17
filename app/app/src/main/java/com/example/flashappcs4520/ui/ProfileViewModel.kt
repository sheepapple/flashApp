package com.example.flashappcs4520.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashappcs4520.common.User
import com.example.flashappcs4520.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Loads the current user's profile from UserRepository.
 * Observed by ProfileScreen to display username and interests.
 */
class ProfileViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            _user.value = repository.fetchCurrentUser()
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            try {
                repository.updateUsername(newUsername)
                _user.value = _user.value?.copy(username = newUsername)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to update username", e)
            }
        }
    }

    fun toggleTopic(topic: String) {
        val current = _user.value?.interests ?: emptyList()
        val updated = if (topic in current) current - topic else current + topic
        viewModelScope.launch {
            try {
                repository.updateInterests(updated)
                _user.value = _user.value?.copy(interests = updated)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to update interests", e)
            }
        }
    }
}