package com.learning.requestletter

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.learning.requestletter.databinding.ActivityMainTambahEditDataBinding
import com.learning.requestletter.databinding.DialogConfirmSubmitBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivityTambahEditData : AppCompatActivity() {

    private lateinit var binding: ActivityMainTambahEditDataBinding
    private val dateFormat     = SimpleDateFormat("dd MMM yyyy", Locale("id"))
    private val isoDateFormat  = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Mode & data edit
    private var isEditMode = false
    private var editPosition = -1
    private var existingItem: PengajuanItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainTambahEditDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Deteksi mode dari Intent
        isEditMode   = intent.getStringExtra(MainActivityDashBoard.EXTRA_MODE) == MainActivityDashBoard.MODE_EDIT
        editPosition = intent.getIntExtra(MainActivityDashBoard.EXTRA_POSITION, -1)
        existingItem = intent.getSerializableExtra(MainActivityDashBoard.EXTRA_ITEM) as? PengajuanItem

        setupSpinners()
        setupDatePickers()
        setupButtons()

        // Kondisi title & pre-fill
        if (isEditMode && existingItem != null) {
            binding.tvFormTitle.text = "Edit Pengajuan"
            binding.btnGenerate.text = "Simpan Perubahan"
            prefillData(existingItem!!)
        } else {
            binding.tvFormTitle.text = "Pengajuan Baru"
            binding.btnGenerate.text = getString(R.string.generate_document)
        }
    }

    // ── Pre-fill data saat mode Edit ──────────────────────────────────────────
    private fun prefillData(item: PengajuanItem) {
        binding.etNim.setText(item.nim)
        binding.etFullName.setText(item.namaLengkap)
        binding.etTargetInstitution.setText(item.instansiTujuan)
        binding.tvStartDate.text = item.tanggalMulai
        binding.tvEndDate.text   = item.tanggalSelesai

        // Set spinner Kategori
        val kategoriList = resources.getStringArray(R.array.kategori_array)
        val kategoriIndex = kategoriList.indexOf(item.kategori)
        if (kategoriIndex >= 0) binding.spinnerKategori.setSelection(kategoriIndex)

        // Set spinner Status
        val statusLabel = when (item.status) {
            "Approved" -> "Disetujui"
            "Rejected" -> "Tidak Disetujui"
            else       -> "Proses"
        }
        val statusList  = resources.getStringArray(R.array.status_array)
        val statusIndex = statusList.indexOf(statusLabel)
        if (statusIndex >= 0) binding.spinnerStatus.setSelection(statusIndex)
    }

    // ── Spinners ──────────────────────────────────────────────────────────────
    private fun setupSpinners() {
        binding.spinnerKategori.adapter =
            HintSpinnerAdapter(this, resources.getStringArray(R.array.kategori_array).toList())
        binding.spinnerKategori.setPopupBackgroundResource(R.drawable.bg_spinner_popup)

        binding.spinnerStatus.adapter =
            HintSpinnerAdapter(this, resources.getStringArray(R.array.status_array).toList())
        binding.spinnerStatus.setPopupBackgroundResource(R.drawable.bg_spinner_popup)
    }

    // ── DatePickers ───────────────────────────────────────────────────────────
    private fun setupDatePickers() {
        (binding.tvStartDate.parent as android.view.View)
            .setOnClickListener { showDatePicker(binding.tvStartDate) }
        binding.tvStartDate.setOnClickListener { showDatePicker(binding.tvStartDate) }
        binding.ivStartDateIcon.setOnClickListener { showDatePicker(binding.tvStartDate) }

        (binding.tvEndDate.parent as android.view.View)
            .setOnClickListener { showDatePicker(binding.tvEndDate) }
        binding.tvEndDate.setOnClickListener { showDatePicker(binding.tvEndDate) }
        binding.ivEndDateIcon.setOnClickListener { showDatePicker(binding.tvEndDate) }
    }

    // ── Buttons ───────────────────────────────────────────────────────────────
    private fun setupButtons() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnGenerate.setOnClickListener {
            val nim            = binding.etNim.text.toString().trim()
            val namaLengkap    = binding.etFullName.text.toString().trim()
            val instansi       = binding.etTargetInstitution.text.toString().trim()
            val tanggalMulai   = binding.tvStartDate.text.toString().trim()
            val tanggalSelesai = binding.tvEndDate.text.toString().trim()
            val kategori       = binding.spinnerKategori.selectedItem.toString()
            val status         = binding.spinnerStatus.selectedItem.toString()

            when {
                nim.isEmpty() || namaLengkap.isEmpty() || instansi.isEmpty() ->
                    toast("Harap lengkapi semua data!")
                binding.spinnerKategori.selectedItemPosition == 0 ->
                    toast("Pilih kategori terlebih dahulu!")
                tanggalMulai.isEmpty() ->
                    toast("Pilih tanggal mulai!")
                tanggalSelesai.isEmpty() ->
                    toast("Pilih tanggal selesai!")
                binding.spinnerStatus.selectedItemPosition == 0 ->
                    toast("Pilih status terlebih dahulu!")
                else -> showConfirmDialog(
                    nim, namaLengkap, instansi, kategori,
                    tanggalMulai, tanggalSelesai, status
                )
            }
        }
    }

    // ── Confirm Dialog ────────────────────────────────────────────────────────
    private fun showConfirmDialog(
        nim: String, namaLengkap: String, instansi: String,
        kategori: String, tanggalMulai: String, tanggalSelesai: String, status: String
    ) {
        val dialogBinding = DialogConfirmSubmitBinding.inflate(LayoutInflater.from(this))

        // Isi ringkasan data
        dialogBinding.tvConfirmNim.text        = nim
        dialogBinding.tvConfirmNama.text       = namaLengkap
        dialogBinding.tvConfirmKategori.text   = kategori
        dialogBinding.tvConfirmStatus.text     = status
        dialogBinding.tvConfirmTglMulai.text   = tanggalMulai
        dialogBinding.tvConfirmTglSelesai.text = tanggalSelesai
        dialogBinding.tvConfirmInstansi.text   = instansi

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        dialog.setCancelable(false)

        dialogBinding.btnDialogKembali.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnDialogKirim.setOnClickListener {
            dialog.dismiss()
            submitForm(nim, namaLengkap, instansi, kategori, tanggalMulai, tanggalSelesai, status)
        }

        dialog.show()
    }

    // ── Submit ────────────────────────────────────────────────────────────────
    private fun submitForm(
        nim: String, namaLengkap: String, instansi: String,
        kategori: String, tanggalMulai: String, tanggalSelesai: String, status: String
    ) {
        val statusValue = when (status) {
            "Disetujui"       -> "Approved"
            "Tidak Disetujui" -> "Rejected"
            else              -> "Pending"
        }

        // Edit: pertahankan ID & tanggal asli; Tambah: buat baru
        val item = PengajuanItem(
            id             = existingItem?.id ?: "REQ-${System.currentTimeMillis().toString().takeLast(6)}",
            kategori       = kategori,
            instansiTujuan = instansi,
            tanggalMulai   = tanggalMulai,
            tanggalSelesai = tanggalSelesai,
            nim            = nim,
            namaLengkap    = namaLengkap,
            tanggalDibuat  = existingItem?.tanggalDibuat ?: isoDateFormat.format(Date()),
            status         = statusValue
        )

        val resultIntent = Intent().apply {
            putExtra(MainActivityDashBoard.EXTRA_ITEM, item)
            if (isEditMode) putExtra(MainActivityDashBoard.EXTRA_POSITION, editPosition)
        }

        setResult(Activity.RESULT_OK, resultIntent)
        toast(if (isEditMode) "Perubahan berhasil disimpan!" else "Pengajuan berhasil dibuat!")
        finish()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun showDatePicker(target: TextView) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                cal.set(year, month, day)
                target.text = dateFormat.format(cal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
