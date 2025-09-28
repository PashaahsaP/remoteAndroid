package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "users",
    foreignKeys = [
        ForeignKey(entity = Credential::class, parentColumns = ["id"], childColumns = ["credentialId"], ForeignKey.CASCADE),

    ])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val credentialId: Long?,
    val other: String? // JSON
)