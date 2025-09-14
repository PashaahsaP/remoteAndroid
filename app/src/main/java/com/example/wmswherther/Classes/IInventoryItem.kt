package com.example.wmsRemote.Classes

interface IInventoryItem {
    val catalogId: Int
    val goodsId: Int
    val cellId: Int
    val barcode: String
    val amount: Pair<Int, Int>
    val type: String
    val supplierId: Int
}
