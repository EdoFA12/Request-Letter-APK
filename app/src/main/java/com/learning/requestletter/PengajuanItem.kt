package com.learning.requestletter

import java.io.Serializable

data class PengajuanItem(
    val id: String,
    val kategori: String,
    val instansiTujuan: String,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val nim: String,
    val namaLengkap: String,
    val tanggalDibuat: String,
    val status: String = "Pending"
) : Serializable

