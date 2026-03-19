package com.acad.smartcv.ui.components

import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.acad.smartcv.R
import com.acad.smartcv.data.model.Organisation
import com.google.android.material.chip.Chip

// ── Generic section card adapter ───────────────────────────────────────────
class SectionCardAdapter<T>(
    private val items: List<T>,
    private val title: (T) -> String,
    private val sub: (T) -> String,
    private val badge: (T) -> String,
    private val badgeColor: Int,
    private val onDelete: (T) -> Unit
) : RecyclerView.Adapter<SectionCardAdapter<T>.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvCardTitle)
        val tvSub: TextView = itemView.findViewById(R.id.tvCardSub)
        val chipBadge: Chip = itemView.findViewById(R.id.chipBadge)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_section_card, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvTitle.text = title(item)
        holder.tvSub.text = sub(item).trim(' ', '·', ' ')
        holder.chipBadge.text = badge(item)
        holder.chipBadge.chipBackgroundColor =
            android.content.res.ColorStateList.valueOf(holder.itemView.context.getColor(badgeColor))
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }
}

// ── Organisation list adapter ──────────────────────────────────────────────
class OrgAdapter(
    private val onApply: (Organisation) -> Unit,
    private val onBookmark: (Organisation, Boolean) -> Unit
) : ListAdapter<Organisation, OrgAdapter.VH>(DIFF) {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvOrgName)
        val tvType: TextView = itemView.findViewById(R.id.tvOrgType)
        val tvField: Chip = itemView.findViewById(R.id.chipField)
        val tvLocation: TextView = itemView.findViewById(R.id.tvOrgLocation)
        val tvDesc: TextView = itemView.findViewById(R.id.tvOrgDesc)
        val btnApply: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btnApply)
        val btnBookmark: ImageButton = itemView.findViewById(R.id.btnBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_organisation, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val org = getItem(position)
        holder.tvName.text = org.name
        holder.tvType.text = org.type
        holder.tvField.text = org.field
        holder.tvLocation.text = org.location
        holder.tvDesc.text = org.description
        holder.btnApply.setOnClickListener { onApply(org) }
        holder.btnBookmark.setImageResource(
            if (org.isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
        )
        holder.btnBookmark.setOnClickListener { onBookmark(org, !org.isBookmarked) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Organisation>() {
            override fun areItemsTheSame(a: Organisation, b: Organisation) = a.id == b.id
            override fun areContentsTheSame(a: Organisation, b: Organisation) = a == b
        }
    }
}
