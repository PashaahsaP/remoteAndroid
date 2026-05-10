package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "users",
    foreignKeys = [
        ForeignKey(entity = Credential::class, parentColumns = ["id"], childColumns = ["credentialId"], ForeignKey.CASCADE),

    ])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val firstName: String,
    val lastName: String,
    val credentialId: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val isDeleted: Boolean,
    val other: String? // JSON
)