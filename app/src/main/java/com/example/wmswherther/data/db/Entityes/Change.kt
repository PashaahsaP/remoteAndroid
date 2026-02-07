package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "changes",
    foreignKeys = [
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE)

    ])
data class Change(
    @PrimaryKey(autoGenerate = false) val id: String,
    val entityId: String,
    val operationType: Int,
    val status: Int,
    val supplierId: String?,
    val other: String? // JSON
)