package com.example.motivationalmornings.Persistence

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Delete
import androidx.room.migration.Migration
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
    @ColumnInfo(name = "reflection") val reflection: String? = null,
    @ColumnInfo(name = "weather") val weather: String? = null
)

@Entity(tableName = "quotes")
data class QuoteOfTheDay(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "text") val text: String,
)

@Entity(tableName = "rss_feed_urls")
data class RssFeedUrl(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "url") val url: String,
)

@Dao
interface DailyContentDao {
    @Query("SELECT text FROM intentions WHERE date = :date ORDER BY uid DESC")
    fun getIntentionsByDate(date: String): Flow<List<String>>

    @Query("SELECT * FROM intentions ORDER BY date DESC, uid DESC")
    fun getAllIntentions(): Flow<List<Intention>>

    @Query("SELECT text FROM quotes ORDER BY RANDOM() LIMIT 1")
    fun getRandomQuote(): Flow<String?>

    @Query("SELECT * FROM quotes ORDER BY uid DESC")
    fun getAllQuotes(): Flow<List<QuoteOfTheDay>>

    @Insert
    suspend fun insertIntention(intention: Intention)

    @Query("UPDATE intentions SET reflection = :reflection WHERE uid = :uid")
    suspend fun updateReflection(uid: Int, reflection: String)

    @Insert
    suspend fun insertQuote(quote: QuoteOfTheDay)

    @Delete
    suspend fun deleteQuote(quote: QuoteOfTheDay)

    @Query("SELECT url FROM rss_feed_urls ORDER BY uid ASC")
    fun getRssFeedUrls(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRssFeedUrl(rssFeedUrl: RssFeedUrl)

    @Query("DELETE FROM rss_feed_urls WHERE url = :url")
    suspend fun deleteRssFeedUrl(url: String)
}

@Database(entities = [Intention::class, QuoteOfTheDay::class, RssFeedUrl::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyContentDao(): DailyContentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS rss_feed_urls (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, url TEXT NOT NULL)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE intentions ADD COLUMN reflection TEXT"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE intentions ADD COLUMN weather TEXT"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motivational_mornings_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
