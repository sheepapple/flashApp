package com.example.flashappcs4520.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashappcs4520.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

/**
 * Handles three user profile update operations: username, password, and avatar URL
 */
class SettingViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _usernameSaveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val usernameSaveState: StateFlow<SaveState> = _usernameSaveState.asStateFlow()

    private val _passwordSaveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val passwordSaveState: StateFlow<SaveState> = _passwordSaveState.asStateFlow()

    private val _avatarSaveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val avatarSaveState: StateFlow<SaveState> = _avatarSaveState.asStateFlow()
    
    fun saveUsername(username: String) {
        if (username.isBlank()) {
            _usernameSaveState.value = SaveState.Error("Username cannot be empty")
            return
        }
        viewModelScope.launch {
            _usernameSaveState.value = SaveState.Loading
            try {
                repository.updateUsername(username)
                _usernameSaveState.value = SaveState.Success
            } catch (e: Exception) {
                Log.e("SettingViewModel", "Failed to update username", e)
                _usernameSaveState.value = SaveState.Error(e.message ?: "Failed to update username")
            }
        }
    }

    fun savePassword(newPassword: String, confirmPassword: String) {
        when {
            newPassword.isBlank() -> {
                _passwordSaveState.value = SaveState.Error("Password cannot be empty")
                return
            }
            // check for match of two new password
            newPassword != confirmPassword -> {
                _passwordSaveState.value = SaveState.Error("Passwords do not match")
                return
            }
            newPassword.length < 6 -> {
                _passwordSaveState.value = SaveState.Error("Password must be at least 6 characters")
                return
            }
        }
        viewModelScope.launch {
            _passwordSaveState.value = SaveState.Loading
            try {
                repository.updatePassword(newPassword)
                _passwordSaveState.value = SaveState.Success
            } catch (e: Exception) {
                Log.e("SettingViewModel", "Failed to update password", e)
                _passwordSaveState.value = SaveState.Error(e.message ?: "Failed to update password")
            }
        }
    }

    fun saveAvatarUrl(url: String) {
        viewModelScope.launch {
            _avatarSaveState.value = SaveState.Loading
            try {
                repository.updateAvatarUrl(url)
                _avatarSaveState.value = SaveState.Success
            } catch (e: Exception) {
                Log.e("SettingViewModel", "Failed to update avatar URL", e)
                _avatarSaveState.value = SaveState.Error(e.message ?: "Failed to update profile picture")
            }
        }
    }

    fun resetUsernameSaveState() { _usernameSaveState.value = SaveState.Idle }
    fun resetPasswordSaveState() { _passwordSaveState.value = SaveState.Idle }
    fun resetAvatarSaveState() { _avatarSaveState.value = SaveState.Idle }
}
