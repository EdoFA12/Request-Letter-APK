package com.learning.requestletter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class HintSpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, R.layout.spinner_item, items) {

    private val inflater = LayoutInflater.from(context)
    private val hintColor  = Color.parseColor("#AAAAAA")
    private val textColor  = Color.parseColor("#1A1A2E")
    private val disabledBg = Color.parseColor("#F5F5F5")

    // Item yang tampil di field (saat collapsed)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.text  = items[position]
        view.setTextColor(if (position == 0) hintColor else textColor)
        view.textSize = 14f
        view.setPadding(4, 0, 4, 0)
        return view
    }

    // Item di dalam popup dropdown
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.spinner_dropdown_item, parent, false)

        val tvText   = view.findViewById<TextView>(R.id.tvDropdownText)
        val vDot     = view.findViewById<View>(R.id.vDot)
        val ivCheck  = view.findViewById<ImageView>(R.id.ivCheckMark)

        tvText.text = items[position]

        if (position == 0) {
            // Hint item — abu-abu, tidak ada dot, tidak aktif
            tvText.setTextColor(hintColor)
            tvText.textSize = 13f
            vDot.visibility  = View.INVISIBLE
            ivCheck.visibility = View.GONE
            view.setBackgroundColor(disabledBg)
            view.isEnabled = false
        } else {
            tvText.setTextColor(textColor)
            tvText.textSize = 14f
            vDot.visibility  = View.VISIBLE
            ivCheck.visibility = View.GONE
            view.background = context.getDrawable(R.drawable.bg_spinner_dropdown_item)
            view.isEnabled = true
        }

        return view
    }

    override fun isEnabled(position: Int) = position != 0
    override fun getCount() = items.size
}



