package com.example.blik

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

object AuthRepository {

    private val auth
        get() = SupabaseProvider.client.auth

    fun existeSessao(): Boolean {
        return auth.currentSessionOrNull() != null
    }

    suspend fun entrar(
        email: String,
        senha: String
    ) {
        auth.signInWith(Email) {
            this.email = email.trim()
            password = senha
        }
    }

    suspend fun cadastrar(
        email: String,
        senha: String
    ) {
        auth.signUpWith(Email) {
            this.email = email.trim()
            password = senha
        }
    }

    suspend fun sair() {
        auth.signOut()
    }
}