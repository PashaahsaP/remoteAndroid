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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long?,
    val name: String,
    val other: String?
)