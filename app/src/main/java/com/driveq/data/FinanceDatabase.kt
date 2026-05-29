package com.driveq.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.driveq.model.GoalHistoryEntry
import com.driveq.model.RideRecord
import com.driveq.model.Transaction
import com.driveq.model.User

@Database(
    entities = [Transaction::class, RideRecord::class, GoalHistoryEntry::class, User::class],
    version = 5,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun rideHistoryDao(): RideHistoryDao
    abstract fun goalHistoryDao(): GoalHistoryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        /**
         * Migração v4 → v5: adiciona colunas googleId e photoUrl, e recria
         * a tabela users para permitir passwordHash nullable (login Google).
         * Preserva dados existentes.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Criar tabela temporária com schema novo
                db.execSQL("""
                    CREATE TABLE users_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL,
                        passwordHash TEXT,
                        googleId TEXT,
                        photoUrl TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // 2. Copiar dados existentes
                db.execSQL("""
                    INSERT INTO users_new (id, name, email, passwordHash, createdAt)
                    SELECT id, name, email, passwordHash, createdAt FROM users
                """.trimIndent())

                // 3. Remover tabela antiga e renomear
                db.execSQL("DROP TABLE users")
                db.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        fun getInstance(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance.db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
