package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "services",
    foreignKeys =
        [
            ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], ForeignKey.CASCADE)

        ])
data class Service (
    @PrimaryKey(autoGenerate = false) val id: String,
    val supplierId: Int?,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val isDeleted: Boolean,
    val other: String?
)