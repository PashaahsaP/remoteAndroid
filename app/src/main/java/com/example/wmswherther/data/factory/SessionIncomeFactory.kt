package com.example.wmswherther.data.factory

import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Entityes.SessionIncome
import java.util.UUID

object SessionIncomeFactory {
    fun create(
        supplierId: String,
        incomeCellId: String?,
        toCellId: String?
    ): SessionIncome{
        return SessionIncome(
            id = UUID.randomUUID().toString(),
            supplierId = supplierId,
            incomeCellId = incomeCellId,
            toCellId = toCellId,
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