package com.example.motivationalmornings.Persistence

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ─── Entities ────────────────────────────────────────────────────────────────

@Entity(tableName = "intentions")
data class Intention(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "reflection") val reflection: String? = null,
    @ColumnInfo(name = "weather") val weather: String? = null,
    @ColumnInfo(name = "time") val time: String? = null
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

@Entity(tableName = "aggregator_source_urls")
data class AggregatorSourceUrl(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "url") val url: String,
)

@Entity(tableName = "tracked_stocks")
data class TrackedStock(
    @PrimaryKey val symbol: String,
)

// NEW ENTITIES
@Entity(tableName = "quote_feedback")
data class QuoteFeedback(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "quote_id") val quoteId: Int,
    @ColumnInfo(name = "reaction") val reaction: String, // "LIKE" or "DISLIKE"
    @ColumnInfo(name = "created_at") val createdAt: String
)

@Entity(tableName = "image_feedback")
data class ImageFeedback(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "image_id") val imageId: Int,
    @ColumnInfo(name = "reaction") val reaction: String, // "LIKE" or "DISLIKE"
    @ColumnInfo(name = "created_at") val createdAt: String
)

/**
 * Represents an image in the image-of-the-day pool.
 *
 * Exactly one of [drawableResId] or [filePath] will be non-null:
 *  - [drawableResId] is set for the built-in seeded images (res/drawable).
 *  - [filePath]      is set for user-submitted images stored in internal storage.
 */
@Entity(tableName = "images_of_the_day")
data class ImageOfTheDay(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    /** Non-null for drawable-backed images (e.g. R.drawable.imageotd). */
    @ColumnInfo(name = "drawable_res_id") val drawableResId: Int? = null,
    /** Non-null for user-uploaded images; absolute path inside filesDir. */
    @ColumnInfo(name = "file_path") val filePath: String? = null,
)

// ─── DAO ─────────────────────────────────────────────────────────────────────

@Dao
interface DailyContentDao {

    // Intentions
    @Query("SELECT text FROM intentions WHERE date = :date ORDER BY uid DESC")
    fun getIntentionsByDate(date: String): Flow<List<String>>

    @Query("SELECT * FROM intentions ORDER BY date DESC, uid DESC")
    fun getAllIntentions(): Flow<List<Intention>>

    @Insert
    suspend fun insertIntention(intention: Intention)

    @Query("UPDATE intentions SET reflection = :reflection WHERE uid = :uid")
    suspend fun updateReflection(uid: Int, reflection: String)

    // Quotes
    @Query("SELECT * FROM quotes ORDER BY uid DESC")
    fun getAllQuotes(): Flow<List<QuoteOfTheDay>>

    @Insert
    suspend fun insertQuote(quote: QuoteOfTheDay)

    @Delete
    suspend fun deleteQuote(quote: QuoteOfTheDay)

    // RSS feed URLs
    @Query("SELECT url FROM rss_feed_urls ORDER BY uid ASC")
    fun getRssFeedUrls(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRssFeedUrl(rssFeedUrl: RssFeedUrl)

    @Query("DELETE FROM rss_feed_urls WHERE url = :url")
    suspend fun deleteRssFeedUrl(url: String)

    // Aggregator section URLs
    @Query("SELECT url FROM aggregator_source_urls ORDER BY uid ASC")
    fun getAggregatorSourceUrls(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAggregatorSourceUrl(source: AggregatorSourceUrl)

    @Query("DELETE FROM aggregator_source_urls WHERE url = :url")
    suspend fun deleteAggregatorSourceUrl(url: String)

    // Stocks
    @Query("SELECT symbol FROM tracked_stocks ORDER BY symbol ASC")
    fun getTrackedStocks(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackedStock(stock: TrackedStock)

    @Query("DELETE FROM tracked_stocks WHERE symbol = :symbol")
    suspend fun deleteTrackedStock(symbol: String)

    // Images of the day
    @Query("SELECT * FROM images_of_the_day ORDER BY uid ASC")
    fun getAllImages(): Flow<List<ImageOfTheDay>>

    @Query("SELECT COUNT(*) FROM images_of_the_day")
    suspend fun getImageCount(): Int

    @Insert
    suspend fun insertImage(image: ImageOfTheDay)

    @Delete
    suspend fun deleteImage(image: ImageOfTheDay)

    // new
    @Insert
    suspend fun insertQuoteFeedback(feedback: QuoteFeedback)

    @Insert
    suspend fun insertImageFeedback(feedback: ImageFeedback)

    @Query("SELECT * FROM quote_feedback ORDER BY uid DESC")
    fun getAllQuoteFeedback(): Flow<List<QuoteFeedback>>

    @Query("SELECT * FROM image_feedback ORDER BY uid DESC")
    fun getAllImageFeedback(): Flow<List<ImageFeedback>>
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [
        Intention::class,
        QuoteOfTheDay::class,
        RssFeedUrl::class,
        AggregatorSourceUrl::class,
        TrackedStock::class,
        ImageOfTheDay::class,
        QuoteFeedback::class,
        ImageFeedback::class,
    ],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyContentDao(): DailyContentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v1 → v2: add rss_feed_urls table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS rss_feed_urls " +
                            "(uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, url TEXT NOT NULL)"
                )
            }
        }

        // v2 → v3: add reflection column to intentions
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE intentions ADD COLUMN reflection TEXT")
            }
        }

        // v3 → v4: add images_of_the_day table and weather column to intentions
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS images_of_the_day (" +
                            "uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "drawable_res_id INTEGER, " +
                            "file_path TEXT" +
                            ")"
                )
                db.execSQL("ALTER TABLE intentions ADD COLUMN weather TEXT")
            }
        }

        // v4 → v5: add feedback tables and time column to intentions
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS quote_feedback (" +
                            "uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "quote_id INTEGER NOT NULL, " +
                            "reaction TEXT NOT NULL, " +
                            "created_at TEXT NOT NULL)"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS image_feedback (" +
                            "uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "image_id INTEGER NOT NULL, " +
                            "reaction TEXT NOT NULL, " +
                            "created_at TEXT NOT NULL)"
                )
                db.execSQL("ALTER TABLE intentions ADD COLUMN time TEXT")
            }
        }

        // v5 → v6: aggregator saved section URLs
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS aggregator_source_urls " +
                            "(uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, url TEXT NOT NULL)"
                )
            }
        }

        // v6 → v7: tracked stocks table
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS tracked_stocks " +
                            "(symbol TEXT PRIMARY KEY NOT NULL)"
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    val dao = database.dailyContentDao()
                                    seedDefaultData(dao)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance

                // Seed images on first run if the table is empty (handles migration from v3).
                CoroutineScope(Dispatchers.IO).launch {
                    seedImagesIfEmpty(instance.dailyContentDao())
                }

                instance
            }
        }

        private suspend fun seedDefaultData(dao: DailyContentDao) {
            val defaultQuotes = listOf(
                "The best way to predict the future is to create it.",
                "Every morning is a new opportunity to become a better version of yourself.",
                "Small steps every day lead to big changes.",
                "You are capable of amazing things today.",
                "Start where you are. Use what you have. Do what you can."
            )
            defaultQuotes.forEach { text -> dao.insertQuote(QuoteOfTheDay(text = text)) }
            seedImagesIfEmpty(dao)
            
            // Seed some default stocks
            listOf("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA").forEach {
                dao.insertTrackedStock(TrackedStock(it))
            }
        }

        /**
         * Seeds the 6 built-in drawable images if the table is empty.
         * Called both on fresh install (onCreate) and after a migration from v3 where
         * the table didn't exist yet.
         */
        suspend fun seedImagesIfEmpty(dao: DailyContentDao) {
            if (dao.getImageCount() == 0) {
                DEFAULT_DRAWABLE_IMAGE_RES_IDS.forEach { resId ->
                    dao.insertImage(ImageOfTheDay(drawableResId = resId))
                }
            }
        }

        /**
         * The 6 built-in res/drawable images. Keep in sync with actual drawable resources.
         */
        val DEFAULT_DRAWABLE_IMAGE_RES_IDS: List<Int> = listOf(
            com.example.motivationalmornings.R.drawable.imageotd,
            com.example.motivationalmornings.R.drawable.imageotd2,
            com.example.motivationalmornings.R.drawable.imageotd3,
            com.example.motivationalmornings.R.drawable.imageotd4,
            com.example.motivationalmornings.R.drawable.imageotd5,
            com.example.motivationalmornings.R.drawable.imageotd6,
        )
    }
}
