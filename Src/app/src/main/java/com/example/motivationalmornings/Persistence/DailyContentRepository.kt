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
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
    @Query("SELECT text FROM intentions WHERE date = :date ORDER BY uid DESC")
    fun getIntentionsByDate(date: String): Flow<List<String>>

    @Query("SELECT text FROM quotes ORDER BY RANDOM() LIMIT 1")
    fun getRandomQuote(): Flow<String?>

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
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    val dao = database.dailyContentDao()
                                    val defaultQuotes = listOf(
                                        "The best way to predict the future is to create it.",
                                        "Every morning is a new opportunity to become a better version of yourself.",
                                        "Small steps every day lead to big changes.",
                                        "You are capable of amazing things today.",
                                        "Start where you are. Use what you have. Do what you can."
                                    )
                                    defaultQuotes.forEach { text ->
                                        dao.insertQuote(QuoteOfTheDay(text = text))
                                    }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
