package com.example.wmswherther.Classes

data class TaskMenuItem(
    var supplier: String,
    var progress: String,
    var number: String,
    var date: String,
)
data class IncomeItem(
    var name: String,
    var TE: String,
    var catalogId: String,
    var haveCount: Int,
    var allCount: Int,
    var teCount:Int = 0,
    var isSelected: Boolean = false,
    var isExpanded: Boolean = false,
    var isExpandable: Boolean = false,
    var isShown: Boolean = true
)
data class MoveItem(
    val name: String,
    val id: String
)
data class MoveSessionItem(
    var name: String,
    var catalogId: String,
    var goodsId: String,
    var allCount: Int,
    var haveCount: Int,
    var isSelected: Boolean,
    var isCell: Boolean,
)
