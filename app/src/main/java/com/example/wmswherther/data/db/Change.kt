package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "changes",
    foreignKeys = [
        ForeignKey(entity = OperationType::class, parentColumns = ["id"], childColumns = ["operationTypeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE)

    ])
data class Change(
    @PrimaryKey(autoGenerate = false) val id: String,
    val entityId: String,
    val operationTypeId: String,
    val status: Int,
    val supplierId: String?,
    val other: String? // JSON
)