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

@Dao
interface IntentionDao {
    @Query("SELECT * FROM Intention")
    fun getAll(): List<Intention>
}