package com.uberfilter.data

import androidx.room.*
import com.uberfilter.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}
