package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "income_items",
    foreignKeys = [
        ForeignKey(entity = SessionIncome::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE)
    ], indices = [Index("sessionId")])
data class IncomeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val goodsId: Long,
    val status: String,
    val other: String? // JSON
)