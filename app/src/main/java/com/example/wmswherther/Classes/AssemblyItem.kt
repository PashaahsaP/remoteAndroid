package com.example.wmsRemote.Classes

class AssemblyItem {
    var sessionId: Int = 0
    var catalogId: Int = 0
    var name: String = ""
    var assemblyItemId: Int = 0
    var supplierId: Int = 0
    var amount: Int = 0
    var cell: String = ""
    var status: Int = 999
    var barcodes: List<String> = listOf()

    constructor(
        sessionId: Int,
        catalogId: Int,
        assemblyItemId: Int,
        supplierId: Int,
        amount: Int,
        cell: String,
        name: String,
        status: Int,
        barcodes: List<String>){
        this.sessionId = sessionId
        this.catalogId = catalogId
        this.assemblyItemId = assemblyItemId
        this.supplierId = supplierId
        this.amount = amount
        this.cell = cell
        this.name = name
        this.status = status
        this.barcodes = barcodes
    }
}