package com.example.assigntodo.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.assigntodo.R
import com.example.assigntodo.models.Works
import com.example.assigntodo.databinding.ItemViewEmployeeWorksBinding
import com.google.android.material.button.MaterialButton
import kotlin.reflect.KFunction2


class EmployeeWorkAdapter(
    private val context: Context,
    private val onProgressButtonClicked: KFunction2<Works, MaterialButton, Unit>? = null,
    private val onCompletedButtonClicked: KFunction2<Works, MaterialButton, Unit>? = null
) : RecyclerView.Adapter<EmployeeWorkAdapter.EmployeeWorkViewHolder>() {
    class EmployeeWorkViewHolder(val binding: ItemViewEmployeeWorksBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object : DiffUtil.ItemCallback<Works>() {
        override fun areItemsTheSame(oldItem: Works, newItem: Works): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Works, newItem: Works): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeWorkViewHolder {
        return EmployeeWorkViewHolder(
            ItemViewEmployeeWorksBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: EmployeeWorkViewHolder, position: Int) {
        val works = differ.currentList[position]
        val isExpanded = works.expanded
        holder.binding.apply {
            tvTitle.text = works.workTitle
            tvDate.text = works.workLastDate
            tvWorkDescriptio.text = works.workDesc
            when (works.workPriority) {
                "1" -> ivOval.setImageResource(R.drawable.green_oval)
                "2" -> ivOval.setImageResource(R.drawable.yellow_oval)
                "3" -> ivOval.setImageResource(R.drawable.red_oval)
            }
            when (works.workStatus) {
                "1" -> holder.binding.tvStatus.text = "Pending"
                "2" -> holder.binding.tvStatus.text = "Progress"
                "3" -> holder.binding.tvStatus.text = "Completed"
            }
            tvWorkDescriptio.visibility = if (isExpanded) View.VISIBLE else View.GONE
            workDescT.visibility = if (isExpanded) View.VISIBLE else View.GONE
            btnProgress.visibility = if (isExpanded) View.VISIBLE else View.GONE
            btnCompleted.visibility = if (isExpanded) View.VISIBLE else View.GONE
            constraintLayout.setOnClickListener {
                isAnyItemExpanded(position)
                works.expanded = !works.expanded
                notifyItemChanged(position, 0)
            }

            if (tvStatus.text == "Progress" || tvStatus.text == "Completed") {
                Log.d("cc", "progress")
                btnProgress.text = "In Progress"
                btnProgress.setTextColor(ContextCompat.getColor(context, R.color.Light5))
            }
            if (tvStatus.text == "Completed") {
                btnCompleted.text = "Work Completed"
                btnCompleted.setTextColor(ContextCompat.getColor(context, R.color.Light5))
            }

            btnProgress.setOnClickListener {
                onProgressButtonClicked?.let { it1 ->
                    it1(
                        works,
                        btnProgress
                    )
                }
            }
            btnCompleted.setOnClickListener {
                onCompletedButtonClicked?.let { it1 ->
                    it1(
                        works,
                        btnCompleted
                    )
                }
            }
        }
    }

    private fun isAnyItemExpanded(position: Int) {
        val expandedItemIndex = differ.currentList.indexOfFirst { it.expanded }
        if (expandedItemIndex >= 0 && expandedItemIndex != position) {
            differ.currentList[expandedItemIndex].expanded = false
            notifyItemChanged(expandedItemIndex, 0)
        }
    }

    override fun onBindViewHolder(
        holder: EmployeeWorkViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads[0] == 0) {
            holder.binding.apply {
                tvWorkDescriptio.visibility = View.GONE
                workDescT.visibility = View.GONE
                btnProgress.visibility = View.GONE
                btnCompleted.visibility = View.GONE
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

}