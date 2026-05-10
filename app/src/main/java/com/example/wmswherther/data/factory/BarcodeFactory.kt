package com.example.wmswherther.data.factory

import com.example.wmswherther.data.db.Entityes.Barcode
import java.util.UUID

object BarcodeFactory {
    fun create(name: String, catalogId: String, supplierId: String) : Barcode{
        return Barcode(
            id = UUID.randomUUID().toString(),
            name = name,
            catalogId = catalogId,
            supplierId = supplierId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}