package com.example.motivationalmornings.Persistence

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "intentions")
data class Intention(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "date") val date: String,
)

@Entity(tableName = "quotes")
data class QuoteOfTheDay(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "text") val text: String,
)

@Dao
interface DailyContentDao {
    @Query("SELECT text FROM intentions ORDER BY uid DESC")
    fun getAllIntentions(): Flow<List<String>>

    @Query("SELECT text FROM quotes ORDER BY uid DESC LIMIT 1")
    fun getLatestQuote(): Flow<String?>

    @Insert
    suspend fun insertIntention(intention: Intention)

    @Insert
    suspend fun insertQuote(quote: QuoteOfTheDay)
}

@Database(entities = [Intention::class, QuoteOfTheDay::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyContentDao(): DailyContentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motivational_mornings_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
