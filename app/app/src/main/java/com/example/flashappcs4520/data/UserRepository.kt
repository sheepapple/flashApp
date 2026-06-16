package com.example.flashappcs4520.data

import com.example.flashappcs4520.common.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fetches the current user's profile from public.users.
 * Called by ProfileViewModel on profile screen load.
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
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: return@withContext
            SupabaseProvider.client
                .from("users")
                .update({ set("username", newUsername) }) {
                    filter { eq("id", uid) }
                }
        }
}