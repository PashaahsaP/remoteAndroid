package com.example.wmswherther.data.factory

import com.example.wmswherther.data.db.Entityes.User

object UserFactory {
    fun create(id: Long, fistName: String, lastName: String, credentialId: Long) : User{
        return User(
            id = id,
            firstName = fistName,
            lastName = lastName,
            credentialId = credentialId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}