package com.learning.requestletter

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.learning.requestletter.databinding.ActivityRiwayatBinding
import com.learning.requestletter.databinding.DialogDeleteConfirmBinding

class RiwayatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatBinding
    private lateinit var adapter: PengajuanAdapter
    private lateinit var db: DatabaseHelper
    private val riwayatList = mutableListOf<PengajuanItem>()

    private var currentKeyword = ""

    // ─── Launcher: Edit ──────────────────────────────────────────────────────

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val item     = result.data?.getSerializableExtra(MainActivityDashBoard.EXTRA_ITEM) as? PengajuanItem
            val position = result.data?.getIntExtra(MainActivityDashBoard.EXTRA_POSITION, -1) ?: -1
            if (item != null && position >= 0) {
                db.update(item)
                loadData()
                // Notify dashboard to refresh
                setResult(Activity.RESULT_OK)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper.getInstance(this)

        setupSearch()
        setupRecyclerView()
        loadData()

        binding.btnBack.setOnClickListener { finish() }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentKeyword = s?.toString()?.trim() ?: ""
                loadData()
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = PengajuanAdapter(
            items    = riwayatList,
            onEdit   = { position, item -> openEditForm(position, item) },
            onDelete = { position -> showDeleteDialog(position) }
        )
        binding.rvRiwayat.layoutManager = LinearLayoutManager(this)
        binding.rvRiwayat.adapter = adapter
    }

    private fun loadData() {
        val data = db.search(currentKeyword, "terbaru")
        riwayatList.clear()
        riwayatList.addAll(data)
        adapter.notifyDataSetChanged()

        val count = riwayatList.size
        binding.tvResultCount.text = "Menampilkan $count pengajuan"

        if (count == 0) {
            binding.rvRiwayat.visibility  = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvRiwayat.visibility  = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    private fun openEditForm(position: Int, item: PengajuanItem) {
        val intent = Intent(this, MainActivityTambahEditData::class.java).apply {
            putExtra(MainActivityDashBoard.EXTRA_MODE, MainActivityDashBoard.MODE_EDIT)
            putExtra(MainActivityDashBoard.EXTRA_ITEM, item)
            putExtra(MainActivityDashBoard.EXTRA_POSITION, position)
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

        dialogBinding.btnDialogBatal.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnDialogHapus.setOnClickListener {
            val itemId = riwayatList[position].id
            db.delete(itemId)
            riwayatList.removeAt(position)
            adapter.notifyItemRemoved(position)
            binding.tvResultCount.text = "Menampilkan ${riwayatList.size} pengajuan"
            if (riwayatList.isEmpty()) {
                binding.rvRiwayat.visibility  = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            }
            setResult(Activity.RESULT_OK)
            dialog.dismiss()
        }

        dialog.show()
    }
}
