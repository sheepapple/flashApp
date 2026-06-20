package com.example.flashappcs4520.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashappcs4520.common.Notification
import com.example.flashappcs4520.data.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _notifications.value = repository.fetchNotifications()
                _hasUnread.value = _notifications.value.any { !it.isRead }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failed to load notifications", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            try {
                repository.markAllRead()
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                _hasUnread.value = false
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failed to mark notifications read", e)
            }
        }
    }
}