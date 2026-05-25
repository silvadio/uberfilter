package com.uberfilter.data

import com.uberfilter.model.User
import java.security.MessageDigest

class UserRepository(private val dao: UserDao) {

    /**
     * Registra um novo usuário.
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
     * Autentica um usuário.
     * Retorna o [User] se credenciais baterem, `null` caso contrário.
     */
    suspend fun login(email: String, password: String): User? {
        val user = dao.findByEmail(email.trim().lowercase()) ?: return null
        val hash = sha256(password)
        return if (user.passwordHash == hash) user else null
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
