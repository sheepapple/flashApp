package com.example.flashappcs4520.data

import android.util.Log
import com.example.flashappcs4520.ui.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fetches the current user's profile from public.users.
 * Called by ProfileViewModel on profile screen load.
 * updates username and areaofinterest
 */
class UserRepository {

    suspend fun fetchCurrentUser(): User? =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: return@withContext null
            SupabaseProvider.client
                .from("users")
                .select {
                    filter { eq("id", uid) }
                    limit(1)
                }
                .decodeList<User>()
                .firstOrNull()
        }

    // writes change to db
    suspend fun updateUsername(newUsername: String) =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
            Log.d("UPDATE_DEBUG", "uid = $uid")
            if (uid == null) return@withContext
            SupabaseProvider.client
                .from("users")
                .update({ set("username", newUsername) }) {
                    filter { eq("id", uid) }
                }
            Log.d("UPDATE_DEBUG", "update call finished")
        }


    suspend fun updateInterests(newInterests: List<String>) =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("No authenticated user")
            SupabaseProvider.client
                .from("users")
                .update({ set("topicofinterest", newInterests) }) {
                    filter { eq("id", uid) }
                }
        }

    // writes change of url to db
    suspend fun updateAvatarUrl(url: String) =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("No authenticated user")
            SupabaseProvider.client
                .from("users")
                .update({ set("profilepicture_url", url) }) {
                    filter { eq("id", uid) }
                }
        }

    // calls Supabase auth.updateUser to update the new password
    suspend fun updatePassword(newPassword: String) =
        withContext(Dispatchers.IO) {
            SupabaseProvider.client.auth.updateUser {
                password = newPassword
            }
        }
}