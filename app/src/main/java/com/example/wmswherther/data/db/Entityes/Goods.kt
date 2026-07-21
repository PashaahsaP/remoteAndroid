package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey

@Entity(
    tableName = "goods",
    foreignKeys = [
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellId"]),
        ForeignKey(entity = Catalog::class, parentColumns = ["id"], childColumns = ["catalogId"])
    ],
    indices = [Index("cellId"), Index("catalogId")]
)

data class Goods(
    @PrimaryKey(autoGenerate = false) val id: String,
    val amount: Int,
    val cellId: String,
    val catalogId: String,
    val createdAt: Long,
    val isAvailable: Boolean,
    val updatedAt: Long,
    val deletedAt: Long?,
    val isDeleted: Boolean,
    val other: String? // JSON
)