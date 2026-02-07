package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "users",
    foreignKeys = [
        ForeignKey(entity = Credential::class, parentColumns = ["id"], childColumns = ["credentialId"], ForeignKey.CASCADE),

    ])
data class User(
    @PrimaryKey(autoGenerate = false) val id: String,
    val firstName: String,
    val lastName: String,
    val credentialId: String?,
    val other: String? // JSON
)