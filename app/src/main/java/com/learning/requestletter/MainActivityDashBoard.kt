package com.learning.requestletter

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.learning.requestletter.databinding.ActivityMainBinding
import com.learning.requestletter.databinding.DialogDeleteConfirmBinding

class MainActivityDashBoard : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PengajuanAdapter
    private lateinit var db: DatabaseHelper

    // Hanya 2 item terbaru untuk ditampilkan di dashboard
    private val previewList = mutableListOf<PengajuanItem>()

    companion object {
        const val EXTRA_ITEM     = "PENGAJUAN_ITEM"
        const val EXTRA_MODE     = "MODE"
        const val EXTRA_POSITION = "POSITION"
        const val MODE_TAMBAH    = "TAMBAH"
        const val MODE_EDIT      = "EDIT"
        private const val DASHBOARD_PREVIEW_COUNT = 2
    }

    // Launcher: Tambah data baru
    private val tambahLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val item = result.data?.getSerializableExtra(EXTRA_ITEM) as? PengajuanItem
            item?.let {
                db.insert(it)
                refreshPreview()
            }
        }
    }

    // Launcher: Edit data yang sudah ada
    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val item     = result.data?.getSerializableExtra(EXTRA_ITEM) as? PengajuanItem
            val position = result.data?.getIntExtra(EXTRA_POSITION, -1) ?: -1
            if (item != null && position >= 0) {
                db.update(item)
                refreshPreview()
            }
        }
    }

    // Launcher: Buka Riwayat (refresh saat kembali jika ada perubahan)
    private val riwayatLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshPreview()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper.getInstance(this)

        setupRecyclerView()
        refreshPreview()

        // FAB → Tambah baru
        binding.fabNewRequest.setOnClickListener {
            val intent = Intent(this, MainActivityTambahEditData::class.java)
            intent.putExtra(EXTRA_MODE, MODE_TAMBAH)
            tambahLauncher.launch(intent)
        }

        // Lihat Semua → Buka RiwayatActivity
        binding.tvLihatSemua.setOnClickListener {
            riwayatLauncher.launch(Intent(this, RiwayatActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = PengajuanAdapter(
            items    = previewList,
            onEdit   = { position, item -> openEditForm(position, item) },
            onDelete = { position -> showDeleteDialog(position) }
        )
        binding.rvPengajuan.layoutManager = LinearLayoutManager(this)
        binding.rvPengajuan.adapter = adapter
    }

    /** Ambil 2 data terbaru dari DB dan tampilkan di dashboard */
    private fun refreshPreview() {
        val allData = db.getAll()
        previewList.clear()
        previewList.addAll(allData.take(DASHBOARD_PREVIEW_COUNT))
        adapter.notifyDataSetChanged()
        binding.rvPengajuan.scrollToPosition(0)
        updateTotalCount(allData.size)
    }

    private fun openEditForm(position: Int, item: PengajuanItem) {
        val intent = Intent(this, MainActivityTambahEditData::class.java).apply {
            putExtra(EXTRA_MODE, MODE_EDIT)
            putExtra(EXTRA_ITEM, item)
            putExtra(EXTRA_POSITION, position)
        }
        editLauncher.launch(intent)
    }

    private fun showDeleteDialog(position: Int) {
        val dialogBinding = DialogDeleteConfirmBinding.inflate(LayoutInflater.from(this))

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialogBinding.btnDialogBatal.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnDialogHapus.setOnClickListener {
            val itemId = previewList[position].id
            db.delete(itemId)
            refreshPreview()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateTotalCount(total: Int) {
        binding.tvTotalCount.text = total.toString()
    }
}


