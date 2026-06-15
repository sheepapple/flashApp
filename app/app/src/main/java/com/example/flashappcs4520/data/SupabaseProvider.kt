package com.example.flashappcs4520.data

import com.example.flashappcs4520.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

/**
 * 1. What: The single, shared Supabase client for the whole app.
 * 2. Who:  Used by repositories (e.g. ArticleRepository) to read from the database.
 * 3. When: Created once, lazily, the first time `client` is accessed.
 *
 * Credentials come from BuildConfig, which is populated from local.properties
 * (gitignored) at build time. We install only Postgrest here because all we do
 * right now is read rows from tables; auth/storage can be added later.
 */
object SupabaseProvider {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Postgrest)
        install(Auth)
    }
}