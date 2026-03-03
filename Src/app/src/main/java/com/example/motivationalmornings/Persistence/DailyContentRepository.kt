package com.example.motivationalmornings.Persistence
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Query

@Entity
data class Intention(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "date") val date: String,
)

data class QuoteOfTheDay(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "text") val text: String,
)

@Dao
interface DailyContentDao {
    @Query("SELECT * FROM Intention")
    fun getAllIntentions(): List<Intention>

    @Query("SELECT * FROM QuoteOfTheDay")
    fun getAllQuotes(): List<QuoteOfTheDay>
}