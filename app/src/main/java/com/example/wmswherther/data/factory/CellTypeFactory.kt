package com.example.wmswherther.data.factory

import com.example.wmswherther.data.db.Entityes.CellType
import java.util.UUID

object CellTypeFactory {
    fun create(type: String, mask: String) : CellType{
        return CellType(
            id = UUID.randomUUID().toString(),
            type = type,
            mask = mask,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}