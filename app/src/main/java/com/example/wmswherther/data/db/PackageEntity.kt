package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "packages",
    foreignKeys = [
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Catalog::class, parentColumns = ["id"], childColumns = ["catalogId"], onDelete = ForeignKey.CASCADE)
    ])
data class PackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long?,
    val name: String?,
    val baseAmount: Int?,
    val catalogId: Long?,
    val weight: Double?,
    val height: Double?,
    val width: Double?,
    val volume: Double?,
    val other: String? // JSON
)