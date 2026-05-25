package com.uberfilter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uberfilter.model.GoalHistoryEntry
import com.uberfilter.model.RideRecord
import com.uberfilter.model.Transaction
import com.uberfilter.model.User

@Database(
    entities = [Transaction::class, RideRecord::class, GoalHistoryEntry::class, User::class],
    version = 4,
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

        fun getInstance(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
