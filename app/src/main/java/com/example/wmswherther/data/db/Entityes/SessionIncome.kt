package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions_income",
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
            childColumns = ["incomeCellId"],
            ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Cell::class,
            parentColumns = ["id"],
            childColumns = ["toCellId"],
            ForeignKey.CASCADE
        )


    ],

)

data class SessionIncome(
    @PrimaryKey(autoGenerate = false) val id: String,
    val supplierId: Int?,
    val incomeCellId: String?,
    val toCellId: String?,
    val status: Int,
    val createdAt: Long,
    val startedAt: Long?,
    val finishedAt: Long?,
    val updatedAt: Long,
    val deletedAt: Long?,
    val isDeleted: Boolean,
    val other: String? // JSON
)