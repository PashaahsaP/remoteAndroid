package com.example.wmswherther.data.db

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
    val supplierId: String?,
    val name: String,
    val other: String?
)