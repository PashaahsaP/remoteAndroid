package com.example.wmswherther.data.factory

import com.example.wmswherther.data.db.Entityes.Supplier
import java.util.UUID

object SupplierFactory {
    fun create(name: String) : Supplier{
       return Supplier(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}