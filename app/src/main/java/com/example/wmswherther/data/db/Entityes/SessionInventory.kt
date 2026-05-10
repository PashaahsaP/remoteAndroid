package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions_inventory",
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Cell::class,
            parentColumns = ["id"],
            childColumns = ["cellId"],
            ForeignKey.CASCADE
        )


    ]
)
data class SessionInventory(
    @PrimaryKey(autoGenerate = false) val id: String,
    val supplierId: String?,
    val cellId: String?,
    val prevSessionId: String?,
    val status: Int,
    val createdAt: Long,
    val startedAt: Long?,
    val finishedAt: Long?,
    val updatedAt: Long,
    val deletedAt: Long?,
    val isDeleted: Boolean,
    val other: String? // JSON
)