package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(tableName = "operation_types",
    foreignKeys = [
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE)
    ])
data class OperationType(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val supplierId: String?
)