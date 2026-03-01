package com.learning.requestletter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.learning.requestletter.databinding.ItemPengajuanBinding

class PengajuanAdapter(
    private val items: MutableList<PengajuanItem>,
    private val onEdit: (position: Int, item: PengajuanItem) -> Unit,
    private val onDelete: (position: Int) -> Unit
) : RecyclerView.Adapter<PengajuanAdapter.ViewHolder>() {

    // ─────────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ─────────────────────────────────────────────────────────────────────────

    inner class ViewHolder(
        val binding: ItemPengajuanBinding
    ) : RecyclerView.ViewHolder(binding.root)

    // ─────────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPengajuanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val ctx  = holder.itemView.context
        val b    = holder.binding

        b.tvItemKategori.text = item.kategori
        b.tvItemInstansi.text = item.instansiTujuan
        b.tvItemId.text       = "ID: #${item.id}"
        b.tvItemTanggal.text  = item.tanggalMulai

        when (item.status) {
            "Approved" -> {
                b.tvItemStatus.text = "✓ Disetujui"
                b.tvItemStatus.setTextColor(ContextCompat.getColor(ctx, R.color.colorApproved))
                b.tvItemStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_approved)
                b.fIconBg.background = ContextCompat.getDrawable(ctx, R.drawable.bg_icon_circle_green)
                b.ivItemIcon.setImageResource(R.drawable.ic_school)
                b.ivItemIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.colorApproved)
                )
            }
            "Rejected" -> {
                b.tvItemStatus.text = "✕ Ditolak"
                b.tvItemStatus.setTextColor(ContextCompat.getColor(ctx, R.color.colorRejected))
                b.tvItemStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_rejected)
                b.fIconBg.background = ContextCompat.getDrawable(ctx, R.drawable.bg_icon_circle_red)
                b.ivItemIcon.setImageResource(R.drawable.ic_assignment)
                b.ivItemIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.colorRejected)
                )
            }
            else -> {
                b.tvItemStatus.text = "⏳ Proses"
                b.tvItemStatus.setTextColor(ContextCompat.getColor(ctx, R.color.colorPending))
                b.tvItemStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_pending)
                b.fIconBg.background = ContextCompat.getDrawable(ctx, R.drawable.bg_icon_circle_blue)
                b.ivItemIcon.setImageResource(R.drawable.ic_description)
                b.ivItemIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.colorAccent)
                )
            }
        }

        b.btnEdit.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_ID.toInt()) onEdit(pos, items[pos])
        }

        b.btnDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_ID.toInt()) onDelete(pos)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public CRUD helpers
    // ─────────────────────────────────────────────────────────────────────────

    fun addItem(item: PengajuanItem) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    fun updateItem(position: Int, item: PengajuanItem) {
        items[position] = item
        notifyItemChanged(position)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}
