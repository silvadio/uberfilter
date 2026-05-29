package com.driveq.data

import androidx.room.*
import com.driveq.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE googleId = :googleId LIMIT 1")
    suspend fun findByGoogleId(googleId: String): User?

    @Query("UPDATE users SET googleId = :googleId, photoUrl = :photoUrl WHERE id = :userId")
    suspend fun updateGoogleInfo(userId: Long, googleId: String, photoUrl: String?)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}
