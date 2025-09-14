package com.example.wmsRemote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "barcode_bork",
    foreignKeys = [
    ForeignKey(entity = CatalogBork::class, parentColumns = ["id"], childColumns = ["catalogId"], onDelete = ForeignKey.CASCADE)],
    indices = [
        Index(value = ["name"], name = "index_barcode_bork_name", unique = false)
    ]
    )
data class BarcodeBork (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "type")
    var type: String?,
    @ColumnInfo(name = "catalogId")
    var catalogId: Int

    )