package com.example.wmsRemote.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [
    Cell::class,
    CatalogAtomy::class,
    CatalogBork::class,
    GoodsAtomy::class,
    GoodsBork::class,
    AssemblySession::class,
    AssemblyBorkItem::class,
    AssemblyAtomyItem::class,
    Shipment::class,
    BarcodeBork::class
                     ], version = 3)
abstract class MainDB :RoomDatabase(){

    abstract  fun getDao() : Dao

    companion object{
        fun getDB(context: Context) : MainDB {
            return Room.databaseBuilder(
                context.applicationContext,
                MainDB::class.java,
                name="Wms.db"
            ).addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3
            )
                //.fallbackToDestructiveMigration()45345345  99999999999999999
                .build()
        }

    }
}
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the 'cells' table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cells (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """
        )
        // Create the 'catalog' table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS catalog_atomy (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                firstBarcode TEXT NOT NULL,
                secondBarcode TEXT
            )
        """
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS catalog_bork (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """
        )
        // Create the 'goods' table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goods_atomy (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                catalogId INTEGER NOT NULL,
                cellId INTEGER NOT NULL,
                amount INTEGER NOT NULL,
                TE TEXT NOT NULL,
                date TEXT NOT NULL,
                createdAt TEXT NOT NULL,
                FOREIGN KEY (catalogId) REFERENCES catalog_atomy(id) ON DELETE CASCADE,
                FOREIGN KEY (cellId) REFERENCES cells(id) ON DELETE CASCADE
            )
        """
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goods_bork (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                catalogId INTEGER NOT NULL,
                cellId INTEGER NOT NULL,
                amount INTEGER NOT NULL,
                createdAt TEXT NOT NULL,
                FOREIGN KEY (catalogId) REFERENCES catalog_bork(id) ON DELETE CASCADE,
                FOREIGN KEY (cellId) REFERENCES cells(id) ON DELETE CASCADE
            )
        """
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS barcode_bork (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                catalogId INTEGER NOT NULL,
                name TEXT NOT NULL,
                type TEXT,
                FOREIGN KEY (catalogId) REFERENCES catalog_bork(id) ON DELETE CASCADE,
            )
        """
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_goods_atomy_TE ON goods_atomy(TE)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_barcode_bork_name ON barcode_bork(name)")
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the 'supplier' table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS assembly_session (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                status INTEGER NOT NULL,
                supplier INTEGER NOT NULL,
                amount INTEGER NOT NULL,
                createdAt TEXT NOT NULL,
                finishedAt TEXT NOT NULL,
                out TEXT
            )
            """
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS assembly_bork_item (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                status INTEGER NOT NULL,
                createdAt TEXT NOT NULL,
                finishedAt TEXT NOT NULL,
                FOREIGN KEY (goodsId) REFERENCES goods_bork(id) ON DELETE CASCADE,
                FOREIGN KEY (assemblyId) REFERENCES assembly_session(id) ON DELETE CASCADE
            )
            """//goods
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS assembly_atomy_item (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                status INTEGER NOT NULL,
                createdAt TEXT NOT NULL,
                finishedAt TEXT NOT NULL,
                FOREIGN KEY (goodsId) REFERENCES goods_atomy(id) ON DELETE CASCADE,
                FOREIGN KEY (assemblyId) REFERENCES assembly_session(id) ON DELETE CASCADE
            )
            """//goods
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS shipment (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                status INTEGER NOT NULL,
                out TEXT NOT NULL
                createdAt TEXT NOT NULL,
                finishedAt TEXT NOT NULL,
                FOREIGN KEY (assemblyId) REFERENCES assembly_session(id) ON DELETE CASCADE
            )
            """//goods
        )


    }
}



