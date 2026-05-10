package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "package_entities",
    foreignKeys = [
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Catalog::class, parentColumns = ["id"], childColumns = ["catalogId"], onDelete = ForeignKey.CASCADE)
    ])
data class PackageEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val supplierId: String?,
    val name: String?,
    val baseAmount: Int?,
    val catalogId: String?,
    val weight: Double?,
    val height: Double?,
    val width: Double?,
    val volume: Double?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val isDeleted: Boolean,
    val other: String? // JSON
)