package com.spendsmart.ui.expenses

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.data.entities.Category
import com.spendsmart.data.entities.Expense
import com.spendsmart.databinding.ActivityExpenseListBinding
import com.spendsmart.utils.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseListBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: SpendSmartDatabase
    private lateinit var adapter: ExpenseAdapter

    private var startDate: Long = getStartOfMonth(getCurrentMonth(), getCurrentYear())
    private var endDate: Long = getEndOfMonth(getCurrentMonth(), getCurrentYear())
    private var categories: Map<Long, Category> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = SpendSmartDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Expenses"

        setupRecyclerView()
        setupDateFilters()
        loadCategories()
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onItemClick = { expense ->
                val intent = Intent(this, ExpenseDetailActivity::class.java)
                intent.putExtra("expense_id", expense.id)
                startActivity(intent)
            },
            onEditClick = { expense ->
                val intent = Intent(this, AddExpenseActivity::class.java)
                intent.putExtra("expense_id", expense.id)
                startActivity(intent)
            },
            onDeleteClick = { expense ->
                lifecycleScope.launch {
                    db.expenseDao().deleteExpense(expense)
                    runOnUiThread { showToast("Expense deleted") }
                    loadExpenses()
                }
            },
            getCategoryName = { id -> categories[id]?.name ?: "Uncategorized" },
            getCategoryColor = { id -> categories[id]?.colorHex ?: "#607D8B" }
        )
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter
    }

    private fun setupDateFilters() {
        updateDateDisplay()

        binding.btnStartDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = startDate
            DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance()
                c.set(y, m, d)
                startDate = getStartOfDay(c.timeInMillis)
                updateDateDisplay()
                loadExpenses()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnEndDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = endDate
            DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance()
                c.set(y, m, d)
                endDate = getEndOfDay(c.timeInMillis)
                updateDateDisplay()
                loadExpenses()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateDateDisplay() {
        binding.tvStartDate.text = startDate.toFormattedDate()
        binding.tvEndDate.text = endDate.toFormattedDate()
    }

    private fun loadCategories() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            val catList = db.categoryDao().getCategoriesByUser(userId).first()
            categories = catList.associateBy { it.id }
            loadExpenses()
        }
    }

    private fun loadExpenses() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            val expenses = db.expenseDao().getExpensesByDateRange(userId, startDate, endDate).first()
            val total = expenses.sumOf { it.amount }

            runOnUiThread {
                adapter.submitList(expenses)
                binding.tvTotalExpenses.text = "Total: ${total.toCurrencyString()}"
                binding.tvExpenseCount.text = "${expenses.size} expenses"

                if (expenses.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvExpenses.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvExpenses.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
