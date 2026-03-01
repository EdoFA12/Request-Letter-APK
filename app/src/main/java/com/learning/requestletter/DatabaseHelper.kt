package com.learning.requestletter

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG              = "DatabaseHelper"
        private const val DATABASE_NAME    = "request_letter.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_PENGAJUAN  = "pengajuan"

        private const val COL_ID           = "id"
        private const val COL_KATEGORI     = "kategori"
        private const val COL_INSTANSI     = "instansi_tujuan"
        private const val COL_TGL_MULAI    = "tanggal_mulai"
        private const val COL_TGL_SELESAI  = "tanggal_selesai"
        private const val COL_NIM          = "nim"
        private const val COL_NAMA         = "nama_lengkap"
        private const val COL_TGL_DIBUAT   = "tanggal_dibuat"
        private const val COL_STATUS       = "status"

        private const val SQL_CREATE_TABLE = """
            CREATE TABLE $TABLE_PENGAJUAN (
                $COL_ID          TEXT PRIMARY KEY NOT NULL,
                $COL_KATEGORI    TEXT NOT NULL,
                $COL_INSTANSI    TEXT NOT NULL,
                $COL_TGL_MULAI   TEXT NOT NULL,
                $COL_TGL_SELESAI TEXT NOT NULL,
                $COL_NIM         TEXT NOT NULL,
                $COL_NAMA        TEXT NOT NULL,
                $COL_TGL_DIBUAT  TEXT NOT NULL,
                $COL_STATUS      TEXT NOT NULL DEFAULT 'Pending'
            )
        """

        // Singleton instance
        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context).also { INSTANCE = it }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE)
        Log.d(TAG, "Table $TABLE_PENGAJUAN created")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Upgrading database from v$oldVersion to v$newVersion")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PENGAJUAN")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    fun insert(item: PengajuanItem): Long {
        return try {
            writableDatabase.use { db ->
                db.insertOrThrow(TABLE_PENGAJUAN, null, item.toContentValues())
            }
        } catch (e: Exception) {
            Log.e(TAG, "insert() failed: ${e.message}", e)
            -1L
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    fun getAll(): List<PengajuanItem> {
        val result = mutableListOf<PengajuanItem>()
        readableDatabase.use { db ->
            db.query(
                TABLE_PENGAJUAN,
                null, null, null, null, null,
                "$COL_TGL_DIBUAT DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    result.add(cursor.toPengajuanItem())
                }
            }
        }
        return result
    }

    fun getById(id: String): PengajuanItem? {
        return readableDatabase.use { db ->
            db.query(
                TABLE_PENGAJUAN,
                null,
                "$COL_ID = ?",
                arrayOf(id),
                null, null, null
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.toPengajuanItem() else null
            }
        }
    }

    fun getCount(): Int {
        return readableDatabase.use { db ->
            db.rawQuery("SELECT COUNT(*) FROM $TABLE_PENGAJUAN", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
        }
    }

    /**
     * Query with optional search keyword and sort option.
     * @param keyword search text (searches nama, instansi, kategori)
     * @param sortBy  one of: "terbaru", "terlama", "nama_az", "nama_za", "kategori"
     */
    fun search(keyword: String = "", sortBy: String = "terbaru"): List<PengajuanItem> {
        val result = mutableListOf<PengajuanItem>()
        val orderClause = when (sortBy) {
            "terlama"  -> "$COL_TGL_DIBUAT ASC"
            "nama_az"  -> "$COL_NAMA ASC"
            "nama_za"  -> "$COL_NAMA DESC"
            "kategori" -> "$COL_KATEGORI ASC"
            else       -> "$COL_TGL_DIBUAT DESC"
        }
        readableDatabase.use { db ->
            val query: android.database.Cursor
            if (keyword.isBlank()) {
                query = db.query(
                    TABLE_PENGAJUAN, null, null, null, null, null, orderClause
                )
            } else {
                val like = "%$keyword%"
                query = db.query(
                    TABLE_PENGAJUAN, null,
                    "$COL_NAMA LIKE ? OR $COL_INSTANSI LIKE ? OR $COL_KATEGORI LIKE ?",
                    arrayOf(like, like, like),
                    null, null, orderClause
                )
            }
            query.use { cursor ->
                while (cursor.moveToNext()) {
                    result.add(cursor.toPengajuanItem())
                }
            }
        }
        return result
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    fun update(item: PengajuanItem): Int {
        return try {
            writableDatabase.use { db ->
                db.update(
                    TABLE_PENGAJUAN,
                    item.toContentValues(),
                    "$COL_ID = ?",
                    arrayOf(item.id)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "update() failed: ${e.message}", e)
            0
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    fun delete(id: String): Int {
        return try {
            writableDatabase.use { db ->
                db.delete(TABLE_PENGAJUAN, "$COL_ID = ?", arrayOf(id))
            }
        } catch (e: Exception) {
            Log.e(TAG, "delete() failed: ${e.message}", e)
            0
        }
    }

    fun deleteAll(): Int {
        return try {
            writableDatabase.use { db -> db.delete(TABLE_PENGAJUAN, null, null) }
        } catch (e: Exception) {
            Log.e(TAG, "deleteAll() failed: ${e.message}", e)
            0
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapper Extensions (private)
    // ─────────────────────────────────────────────────────────────────────────

    private fun PengajuanItem.toContentValues(): ContentValues = ContentValues().apply {
        put(COL_ID,          id)
        put(COL_KATEGORI,    kategori)
        put(COL_INSTANSI,    instansiTujuan)
        put(COL_TGL_MULAI,   tanggalMulai)
        put(COL_TGL_SELESAI, tanggalSelesai)
        put(COL_NIM,         nim)
        put(COL_NAMA,        namaLengkap)
        put(COL_TGL_DIBUAT,  tanggalDibuat)
        put(COL_STATUS,      status)
    }

    private fun Cursor.toPengajuanItem(): PengajuanItem = PengajuanItem(
        id             = getString(getColumnIndexOrThrow(COL_ID)),
        kategori       = getString(getColumnIndexOrThrow(COL_KATEGORI)),
        instansiTujuan = getString(getColumnIndexOrThrow(COL_INSTANSI)),
        tanggalMulai   = getString(getColumnIndexOrThrow(COL_TGL_MULAI)),
        tanggalSelesai = getString(getColumnIndexOrThrow(COL_TGL_SELESAI)),
        nim            = getString(getColumnIndexOrThrow(COL_NIM)),
        namaLengkap    = getString(getColumnIndexOrThrow(COL_NAMA)),
        tanggalDibuat  = getString(getColumnIndexOrThrow(COL_TGL_DIBUAT)),
        status         = getString(getColumnIndexOrThrow(COL_STATUS))
    )
}
