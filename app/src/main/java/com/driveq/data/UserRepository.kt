package com.driveq.data

import com.driveq.model.User
import java.security.MessageDigest

class UserRepository(private val dao: UserDao) {

    /**
     * Registra um novo usuário por email/senha.
     * Retorna [RegisterResult.Success] ou [RegisterResult.EmailAlreadyUsed].
     */
    suspend fun register(name: String, email: String, password: String): RegisterResult {
        val trimmedEmail = email.trim().lowercase()
        if (dao.findByEmail(trimmedEmail) != null) {
            return RegisterResult.EmailAlreadyUsed
        }
        val user = User(
            name         = name.trim(),
            email        = trimmedEmail,
            passwordHash = sha256(password)
        )
        dao.insert(user)
        return RegisterResult.Success(user)
    }

    /**
     * Autentica um usuário por email/senha.
     * Retorna o [User] se credenciais baterem, `null` caso contrário.
     */
    suspend fun login(email: String, password: String): User? {
        val user = dao.findByEmail(email.trim().lowercase()) ?: return null
        val hash = sha256(password)
        return if (user.passwordHash == hash) user else null
    }

    /**
     * Autentica ou registra automaticamente um usuário via Google.
     *
     * Lógica:
     * 1. Busca por googleId → se existe, retorna o usuário (login).
     * 2. Busca por email → se existe, vincula o googleId ao usuário (linking).
     * 3. Se não existe, cria novo usuário (auto-registro).
     *
     * @return [GoogleSignInResult] com o usuário autenticado/criado.
     */
    suspend fun loginWithGoogle(
        googleId: String,
        email: String,
        name: String,
        photoUrl: String?
    ): GoogleSignInResult {
        val trimmedEmail = email.trim().lowercase()

        // 1. Já tem conta Google vinculada? → login direto
        dao.findByGoogleId(googleId)?.let { user ->
            return GoogleSignInResult.Success(user)
        }

        // 2. Já tem conta com mesmo email (email/senha)? → vincular Google
        dao.findByEmail(trimmedEmail)?.let { existingUser ->
            dao.updateGoogleInfo(existingUser.id, googleId, photoUrl)
            val updatedUser = existingUser.copy(
                googleId = googleId,
                photoUrl = photoUrl
            )
            return GoogleSignInResult.Success(updatedUser)
        }

        // 3. Novo usuário → auto-registro
        val newUser = User(
            name     = name.trim(),
            email    = trimmedEmail,
            googleId = googleId,
            photoUrl = photoUrl
        )
        dao.insert(newUser)
        return GoogleSignInResult.Success(newUser)
    }

    /** Retorna `true` se já existir ao menos um usuário cadastrado. */
    suspend fun hasAnyUser(): Boolean = dao.count() > 0

    companion object {
        private fun sha256(input: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(input.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
    }
}

sealed class RegisterResult {
    data class Success(val user: User) : RegisterResult()
    data object EmailAlreadyUsed : RegisterResult()
}

sealed class GoogleSignInResult {
    data class Success(val user: User) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}
