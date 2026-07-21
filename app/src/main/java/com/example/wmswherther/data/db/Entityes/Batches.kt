package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "batches",
    foreignKeys = [
        ForeignKey(entity = Catalog::class, parentColumns = ["id"], childColumns = ["catalogId"], onDelete = ForeignKey.CASCADE)

    ])

data class Batches(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val name: String,
    val catalogId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val isDeleted: Boolean,
    val other: String? // JSON
)