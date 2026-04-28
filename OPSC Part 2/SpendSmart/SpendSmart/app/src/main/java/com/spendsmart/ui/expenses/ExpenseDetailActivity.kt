package com.spendsmart.ui.expenses

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.databinding.ActivityExpenseDetailBinding
import com.spendsmart.utils.SessionManager
import com.spendsmart.utils.toCurrencyString
import com.spendsmart.utils.toFormattedDate
import kotlinx.coroutines.launch

class ExpenseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseDetailBinding
    private lateinit var db: SpendSmartDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = SpendSmartDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Expense Detail"

        val expenseId = intent.getLongExtra("expense_id", -1L)
        if (expenseId != -1L) loadExpense(expenseId)
    }

    private fun loadExpense(id: Long) {
        lifecycleScope.launch {
            val expense = db.expenseDao().getExpenseById(id) ?: return@launch
            val category = expense.categoryId?.let { db.categoryDao().getCategoryById(it) }

            runOnUiThread {
                binding.tvDetailAmount.text = expense.amount.toCurrencyString()
                binding.tvDetailDescription.text = expense.description
                binding.tvDetailDate.text = expense.date.toFormattedDate()
                binding.tvDetailCategory.text = category?.name ?: "Uncategorized"

                if (expense.startTime.isNotEmpty()) {
                    binding.tvDetailTime.text = "${expense.startTime} - ${expense.endTime}"
                    binding.tvDetailTime.visibility = View.VISIBLE
                    binding.labelTime.visibility = View.VISIBLE
                } else {
                    binding.tvDetailTime.visibility = View.GONE
                    binding.labelTime.visibility = View.GONE
                }

                expense.photoPath?.let { path ->
                    binding.ivDetailReceipt.visibility = View.VISIBLE
                    binding.labelReceipt.visibility = View.VISIBLE
                    Glide.with(this@ExpenseDetailActivity).load(path).into(binding.ivDetailReceipt)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
