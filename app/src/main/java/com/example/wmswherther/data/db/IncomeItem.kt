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
    @PrimaryKey(autoGenerate = false) val id: String,
    val sessionId: String,
    val goodsId: String,
    val status: String,
    val other: String? // JSON
)