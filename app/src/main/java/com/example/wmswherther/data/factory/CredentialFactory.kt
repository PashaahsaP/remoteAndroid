package com.example.wmswherther.data.factory

import com.example.wmswherther.data.db.Entityes.Credential
import java.util.UUID

object CredentialFactory {
    fun create(type: String, id: Long) : Credential{
        return Credential(
            id = id,
            type = type,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}