package com.example.flashappcs4520.data

import android.util.Log
import com.example.flashappcs4520.common.User
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
}