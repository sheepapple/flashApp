package com.example.flashappcs4520.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.builtin.Email

/**
 * Thrown with a short, user-facing message instead of the raw Supabase REST error
 * (which otherwise dumps the full POST endpoint URL, headers and method).
 */
class AuthException(message: String) : Exception(message)

class AuthRepository {

    private val client = SupabaseProvider.client

    suspend fun signUp(email: String, password: String) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        } catch (e: Exception) {
            throw AuthException(signUpErrorMessage(e))
        }
    }

    suspend fun signIn(email: String, password: String) {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        } catch (e: Exception) {
            throw AuthException(signInErrorMessage(e))
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    fun currentUser() = client.auth.currentUserOrNull()

    private fun signInErrorMessage(e: Exception): String {
        val authError = e as? AuthRestException ?: return "Something went wrong. Please try again."
        return when (authError.errorCode) {
            AuthErrorCode.EmailNotConfirmed -> "Please confirm your email address before logging in."
            AuthErrorCode.UserBanned -> "This account has been disabled."
            AuthErrorCode.OverRequestRateLimit -> "Too many attempts. Please wait a moment and try again."
            // Wrong email/password (and anything else we don't specifically handle) is reported
            // generically so we never leak whether an account exists.
            else -> "Invalid login information"
        }
    }

    private fun signUpErrorMessage(e: Exception): String {
        if (e is AuthWeakPasswordException) return weakPasswordMessage
        val authError = e as? AuthRestException ?: return "Something went wrong. Please try again."
        return when (authError.errorCode) {
            AuthErrorCode.WeakPassword -> weakPasswordMessage
            AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists ->
                "An account with this email already exists."
            AuthErrorCode.ValidationFailed -> "Please enter a valid email address."
            AuthErrorCode.SignupDisabled -> "Sign-ups are currently disabled."
            AuthErrorCode.OverEmailSendRateLimit, AuthErrorCode.OverRequestRateLimit ->
                "Too many attempts. Please wait a moment and try again."
            // Some email-validation codes (e.g. "email_address_invalid") aren't in AuthErrorCode,
            // so errorCode is null and we fall back to inspecting the raw error string.
            else -> if (authError.error.contains("email", ignoreCase = true)) {
                "Please enter a valid email address."
            } else {
                "Could not create account. Please try again."
            }
        }
    }

    private val weakPasswordMessage =
        "Password is too weak. Use at least 6 characters."
}