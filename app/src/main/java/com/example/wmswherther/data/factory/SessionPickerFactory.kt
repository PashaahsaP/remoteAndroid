package com.example.wmswherther.data.factory

import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Entityes.SessionPicker
import java.util.UUID

object SessionPickerFactory {
    fun create(supplierId: Int, outCellId: String) : SessionPicker{
        return SessionPicker(
            id = UUID.randomUUID().toString(),
            supplierId = supplierId,
            outCellId = outCellId,
            status = StatusType.Created.ordinal,
            createdAt = System.currentTimeMillis(),
            startedAt = System.currentTimeMillis(),
            finishedAt = null,
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}