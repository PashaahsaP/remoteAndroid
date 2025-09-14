package com.example.wmsRemote.Classes

class BorkInventoryItem : IInventoryItem{
    override var catalogId: Int = 0
    override var goodsId: Int = 0
    override var cellId: Int = 0
    override var barcode: String = ""
    override var amount: Pair<Int, Int> = Pair(0,0)
    override var type: String = ""
    override var supplierId: Int = 0
    var name: String = ""
    constructor(catalogId: Int, goodsId :Int, cellId: Int, barcode: String, amount: Pair<Int, Int>, type: String, supplierId: Int, name: String) {
        this.catalogId = catalogId
        this.goodsId = goodsId
        this.cellId = cellId
        this.barcode = barcode
        this.amount = amount
        this.type = type
        this.supplierId = supplierId
        this.name = name

    }
}