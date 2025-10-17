package com.example.wmswherther.Classes

data class TaskMenuItem(
    var supplier: String,
    var progress: String,
    var number: String,
    var date: String,
)
data class IncomeItem(
    var name: String,
    var catalogId: String,
    var haveCount: Int,
    var allCount: Int
    )