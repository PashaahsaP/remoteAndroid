package com.example.wmsRemote.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Credential
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.IncomeItem
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.OutcomeItem
import com.example.wmswherther.data.db.Entityes.PackageEntity
import com.example.wmswherther.data.db.Entityes.PickerItem
import com.example.wmswherther.data.db.Entityes.Service
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.SessionOutcome
import com.example.wmswherther.data.db.Entityes.SessionPicker
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.data.db.Entityes.TrueSign
import com.example.wmswherther.data.db.Entityes.User

@Database(entities = [

    Barcode::class,
    Catalog::class,
    Cell::class,
    CellType::class,
    Change::class,
    Credential::class,
    Goods::class,
    IncomeItem::class,
    Movement::class,
    OutcomeItem::class,
    PackageEntity::class,
    PickerItem::class,
    Service::class,
    SessionIncome::class,
    SessionOutcome::class,
    SessionPicker::class,
    Supplier::class,
    TrueSign::class,
    User::class,
    InventoryDiffItem::class,
    SessionInventory::class
                     ], version = 1)
abstract class MainDB :RoomDatabase(){

    abstract  fun getDao() : Dao

    companion object{
        fun getDB(context: Context) : MainDB {
            return Room.databaseBuilder(
                context.applicationContext,
                MainDB::class.java,
                name="Wms.db"
            )
                //.fallbackToDestructiveMigration()45345345  99999999999999999
                .build()
        }

    }
}
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // <editor-fold desc="Barcodes">


            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS barcodes (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    catalogId TEXT NOT NULL,
                    supplierId TEXT,
                    other TEXT,
                    FOREIGN KEY(catalogId) REFERENCES Catalog(Id) ON DELETE CASCADE,
                    FOREIGN KEY(supplierId) REFERENCES Supplier(Id) ON DELETE CASCADE
                )
                """
            )
            database.execSQL("CREATE INDEX IF NOT EXISTS index_barcodes_name ON barcodes(name)")
            // </editor-fold>
        // <editor-fold desc="Catalog">
            database.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS catalogs (
                            id TEXT NOT NULL PRIMARY KEY,
                            name TEXT NOT NULL,
                            sku TEXT,
                            supplierId TEXT NOT NULL,
                            other TEXT,
                            FOREIGN KEY(supplierId) REFERENCES Supplier(id) ON DELETE CASCADE
                        )
                        """
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_catalogs_name ON catalogs(name)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_catalogs_sku ON catalogs(sku)")
            // </editor-fold>
        // <editor-fold desc="Cells">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS cells (
                id TEXT NOT NULL PRIMARY KEY,
                typeCellId TEXT NOT NULL,
                parentCellId TEXT,
                name TEXT NOT NULL,
                FOREIGN KEY(typeCellId) REFERENCES CellType(id)
            )
        """)

        // Создаем индексы
        database.execSQL("CREATE INDEX IF NOT EXISTS index_typeCellId ON cells(typeCellId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_name ON cells(name)")
        // </editor-fold>
        // <editor-fold desc="CellTypes">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS cell_types (
                id TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                mask TEXT,
                other TEXT
            )
        """)
        // </editor-fold>
        // <editor-fold desc="Changes">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS changes (
                id TEXT NOT NULL PRIMARY KEY,
                entityId TEXT NOT NULL,
                operationType INTEGER NOT NULL,
                status INTEGER NOT NULL,
                supplierId TEXT,
                other TEXT,
                FOREIGN KEY(supplierId) REFERENCES Supplier(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_changes_operationType ON changes(operationType)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_changes_supplierId ON changes(supplierId)")
        // </editor-fold>
        // <editor-fold desc="Credential">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS credentials (
                id TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                other TEXT
            )
        """)
        // </editor-fold>
        // <editor-fold desc="Goods">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS goods (
                id TEXT NOT NULL PRIMARY KEY,
                amount INTEGER NOT NULL,
                cellId TEXT NOT NULL,
                catalogId TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                isAvailable BOOLEAN NOT NULL,
                other TEXT,
                FOREIGN KEY(cellId) REFERENCES Cell(id),
                FOREIGN KEY(catalogId) REFERENCES Catalog(id)
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_goods_cellId ON goods(cellId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_goods_catalogId ON goods(catalogId)")
        // </editor-fold>
        // <editor-fold desc="IncomeItems">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS income_items (
                id TEXT NOT NULL PRIMARY KEY,
                sessionId TEXT NOT NULL,
                goodsId TEXT NOT NULL,
                status TEXT NOT NULL,
                other TEXT,
                FOREIGN KEY(sessionId) REFERENCES SessionIncome(id) ON DELETE CASCADE
            )
        """)

        database.execSQL("CREATE INDEX IF NOT EXISTS index_income_items_sessionId ON income_items(sessionId)")
        // </editor-fold>
        // <editor-fold desc="InventoryDiffItem">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS inventory_diff_items (
                id TEXT NOT NULL PRIMARY KEY,
                inventorySessionId TEXT NOT NULL,
                catalogId TEXT NOT NULL,
                isTE BOOLEAN NOT NULL,
                barcode TEXT NOT NULL,
                parentCellId TEXT NOT NULL,
                diffCount INTEGER NOT NULL,
                status TEXT NOT NULL,
                other TEXT,
                FOREIGN KEY(inventorySessionId) REFERENCES SessionInventory(id) ON DELETE CASCADE
            )
        """)

        // </editor-fold>
        // <editor-fold desc="Movements">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS movements (
                id TEXT NOT NULL PRIMARY KEY,
                cellFromId TEXT NOT NULL,
                cellToId TEXT NOT NULL,
                qty TEXT NOT NULL,
                catalogId TEXT NOT NULL,
                goodsId TEXT NOT NULL,
                userId TEXT,
                executedAt INTEGER NOT NULL,
                operationType INTEGER NOT NULL,
                FOREIGN KEY(cellFromId) REFERENCES Cell(id),
                FOREIGN KEY(cellToId) REFERENCES Cell(id),
                FOREIGN KEY(userId) REFERENCES User(id),
                FOREIGN KEY(catalogId) REFERENCES Catalog(id),
            )
        """)

        database.execSQL("CREATE INDEX IF NOT EXISTS index_movements_cellFromId ON movements(cellFromId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_movements_cellToId ON movements(cellToId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_movements_userId ON movements(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_movements_operationType ON movements(operationType)")
        // </editor-fold>

        // <editor-fold desc="OutcomeItems">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS outcome_items (
                id TEXT NOT NULL PRIMARY KEY,
                sessionId TEXT NOT NULL,
                goodsId TEXT NOT NULL,
                cellId TEXT,
                status INTEGER NOT NULL,
                other TEXT,
                FOREIGN KEY(sessionId) REFERENCES SessionOutcome(id) ON DELETE CASCADE,
                FOREIGN KEY(goodsId) REFERENCES Goods(id) ON DELETE CASCADE,
                FOREIGN KEY(cellId) REFERENCES Cell(id) ON DELETE CASCADE
            )
        """)

        database.execSQL("CREATE INDEX IF NOT EXISTS index_outcome_items_sessionId ON outcome_items(sessionId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_outcome_items_goodsId ON outcome_items(goodsId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_outcome_items_cellId ON outcome_items(cellId)")
        // </editor-fold>
        // <editor-fold desc="Packages">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS package_entities (
                id TEXT NOT NULL PRIMARY KEY,
                supplierId TEXT,
                name TEXT,
                baseAmount INTEGER,
                weight REAL,
                height REAL,
                width REAL,
                volume REAL,
                other TEXT,
                FOREIGN KEY(supplierId) REFERENCES Supplier(id) ON DELETE CASCADE,
            )
        """)

        // Создаем индексы для внешних ключей
        database.execSQL("CREATE INDEX IF NOT EXISTS index_packages_supplierId ON packages(supplierId)")
        // </editor-fold>
        // <editor-fold desc="PickerItems">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS picker_items (
                id TEXT NOT NULL PRIMARY KEY,
                sessionId TEXT NOT NULL,
                goodsId TEXT NOT NULL,
                cellId TEXT,
                status INTEGER NOT NULL,
                startedAt INTEGER,
                finishedAt INTEGER,
                other TEXT,
                FOREIGN KEY(sessionId) REFERENCES SessionPicker(id) ON DELETE CASCADE,
                FOREIGN KEY(goodsId) REFERENCES Goods(id) ON DELETE CASCADE,
                FOREIGN KEY(cellId) REFERENCES Cell(id) ON DELETE CASCADE
            )
        """)

        // Создаем индексы для быстрого поиска по внешним ключам
        database.execSQL("CREATE INDEX IF NOT EXISTS index_picker_items_sessionId ON picker_items(sessionId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_picker_items_goodsId ON picker_items(goodsId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_picker_items_cellId ON picker_items(cellId)")
        // </editor-fold>
        // <editor-fold desc="Services">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS services (
                id TEXT NOT NULL PRIMARY KEY,
                supplierId TEXT,
                name TEXT NOT NULL,
                other TEXT,
                FOREIGN KEY(supplierId) REFERENCES Supplier(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_services_supplierId ON services(supplierId)")
        // </editor-fold>
        // <editor-fold desc="IncomeSessions">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS sessions_income (
                id TEXT NOT NULL PRIMARY KEY,
                supplierId TEXT,
                incomeCellId TEXT,
                toCellId TEXT,
                status INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                startedAt INTEGER,
                finishedAt INTEGER,
                other TEXT,
                FOREIGN KEY(supplierId) REFERENCES Supplier(id) ON DELETE CASCADE,
                FOREIGN KEY(incomeCellId) REFERENCES Cell(id) ON DELETE CASCADE,
                FOREIGN KEY(toCellId) REFERENCES Cell(id) ON DELETE CASCADE
            )
        """)

        // Индексы для оптимизации запросов по внешним ключам
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_income_supplierId ON sessions_income(supplierId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_income_incomeCellId ON sessions_income(incomeCellId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_income_toCellId ON sessions_income(toCellId)")
        // </editor-fold>
        // <editor-fold desc="InventorySession">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS sessions_inventory (
                id TEXT NOT NULL PRIMARY KEY,
                supplierId TEXT,
                cellId TEXT,
                prevSessionId TEXT,
                status INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                startedAt INTEGER,
                finishedAt INTEGER,
                other TEXT,
                FOREIGN KEY(supplierId) REFERENCES Supplier(id) ON DELETE CASCADE,
                FOREIGN KEY(cellId) REFERENCES Cell(id) ON DELETE CASCADE
            )
        """)
        // </editor-fold>
        // <editor-fold desc="OutcomeSessions">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS sessions_outcome (
                id TEXT NOT NULL PRIMARY KEY,
                supplierId TEXT,
                toCellId TEXT,
                outCellId TEXT,
                status INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                startedAt INTEGER,
                finishedAt INTEGER,
                pickerSessionId TEXT,
                other TEXT,
                FOREIGN KEY(supplierId) REFERENCES Supplier(id) ON DELETE CASCADE,
                FOREIGN KEY(toCellId) REFERENCES Cell(id) ON DELETE CASCADE,
                FOREIGN KEY(outCellId) REFERENCES Cell(id) ON DELETE CASCADE,
                FOREIGN KEY(pickerSessionId) REFERENCES SessionPicker(id) ON DELETE CASCADE
            )
        """.trimIndent())

            database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_outcome_supplierId ON sessions_outcome(supplierId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_outcome_toCellId ON sessions_outcome(toCellId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_outcome_outCellId ON sessions_outcome(outCellId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_outcome_pickerSessionId ON sessions_outcome(pickerSessionId)")
        // </editor-fold>
        // <editor-fold desc="PickerSession">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS sessions_picker (
                id TEXT NOT NULL PRIMARY KEY,
                supplierId TEXT,
                outCellId TEXT,
                status TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                startedAt INTEGER,
                finishedAt INTEGER,
                other TEXT,
                FOREIGN KEY(supplierId) REFERENCES Supplier(id) ON DELETE CASCADE,
                FOREIGN KEY(outCellId) REFERENCES Cell(id) ON DELETE CASCADE
            )
        """)

        database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_picker_supplierId ON sessions_picker(supplierId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_picker_outCellId ON sessions_picker(outCellId)")
        // </editor-fold>
        // <editor-fold desc="Suppliers">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS suppliers (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                other TEXT
            )
        """.trimIndent())
        // </editor-fold>
        // <editor-fold desc="TrueSings">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS true_signs (
                id TEXT NOT NULL PRIMARY KEY,
                goodsId TEXT NOT NULL,
                catalogId TEXT NOT NULL,
                name TEXT,
                other TEXT,
                FOREIGN KEY(goodsId) REFERENCES Goods(id) ON DELETE CASCADE,
                FOREIGN KEY(catalogId) REFERENCES Catalog(id) ON DELETE CASCADE
            )
        """)

        database.execSQL("CREATE INDEX IF NOT EXISTS index_true_signs_goodsId ON true_signs(goodsId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_true_signs_catalogId ON true_signs(catalogId)")
       // </editor-fold>
        // <editor-fold desc="Users">
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS users (
                id TEXT NOT NULL PRIMARY KEY,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                credentialId TEXT,
                other TEXT,
                FOREIGN KEY(credentialId) REFERENCES Credential(id) ON DELETE CASCADE
            )
        """)

        database.execSQL("CREATE INDEX IF NOT EXISTS index_users_credentialId ON users(credentialId)")
        // </editor-fold>
    }
}


