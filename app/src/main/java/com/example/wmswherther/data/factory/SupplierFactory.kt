package com.example.wmswherther.data.factory

import com.example.wmswherther.data.db.Entityes.Supplier
import java.util.UUID

object SupplierFactory {
    fun create(name: String, id: Int) : Supplier{
       return Supplier(
            id = id,
            name = name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}