package com.spendsmart.ui.expenses

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spendsmart.data.entities.Expense
import com.spendsmart.databinding.ItemExpenseBinding
import com.spendsmart.utils.toFormattedDate
import com.spendsmart.utils.toCurrencyString

class ExpenseAdapter(
    private val onItemClick: (Expense) -> Unit,
    private val onEditClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit,
    private val getCategoryName: (Long?) -> String,
    private val getCategoryColor: (Long?) -> String
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Expense, newItem: Expense) = oldItem == newItem
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.tvAmount.text = expense.amount.toCurrencyString()
            binding.tvDescription.text = expense.description
            binding.tvDate.text = expense.date.toFormattedDate()
            binding.tvCategory.text = getCategoryName(expense.categoryId)

            try {
                binding.viewCategoryColor.setBackgroundColor(Color.parseColor(getCategoryColor(expense.categoryId)))
            } catch (e: Exception) {
                binding.viewCategoryColor.setBackgroundColor(Color.GRAY)
            }

            binding.ivReceiptIndicator.visibility = if (expense.photoPath != null) View.VISIBLE else View.GONE

            if (expense.startTime.isNotEmpty() && expense.endTime.isNotEmpty()) {
                binding.tvTime.visibility = View.VISIBLE
                binding.tvTime.text = "${expense.startTime} - ${expense.endTime}"
            } else {
                binding.tvTime.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(expense) }
            binding.btnEdit.setOnClickListener { onEditClick(expense) }
            binding.btnDelete.setOnClickListener { onDeleteClick(expense) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
