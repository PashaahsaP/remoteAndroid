package com.example.wmswherther.data.factory

import com.example.wmswherther.data.db.Entityes.Catalog
import java.util.UUID

object CatalogFactory {
    fun create(name: String, sku: String, supplierId: Int) : Catalog{
        return Catalog(
            id = UUID.randomUUID().toString(),
            name = name,
            sku = sku,
            supplierId = supplierId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}