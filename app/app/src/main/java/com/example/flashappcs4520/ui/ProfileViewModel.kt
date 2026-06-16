package com.example.flashappcs4520.ui

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
            repository.updateUsername(newUsername)
            _user.value = _user.value?.copy(username = newUsername)
        }
    }
}