package com.spendsmart.ui.reports

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.databinding.ActivityReportsBinding
import com.spendsmart.utils.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: SpendSmartDatabase

    private var startDate = getStartOfMonth(getCurrentMonth(), getCurrentYear())
    private var endDate = getEndOfMonth(getCurrentMonth(), getCurrentYear())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = SpendSmartDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reports & Insights"

        setupDateFilters()
        loadReports()
    }

    private fun setupDateFilters() {
        updateDateDisplay()

        binding.btnReportStartDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = startDate
            DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance(); c.set(y, m, d)
                startDate = getStartOfDay(c.timeInMillis)
                updateDateDisplay(); loadReports()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnReportEndDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = endDate
            DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance(); c.set(y, m, d)
                endDate = getEndOfDay(c.timeInMillis)
                updateDateDisplay(); loadReports()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateDateDisplay() {
        binding.tvReportStartDate.text = startDate.toFormattedDate()
        binding.tvReportEndDate.text = endDate.toFormattedDate()
    }

    private fun loadReports() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            val categorySpending = db.expenseDao().getSpendingByCategory(userId, startDate, endDate)
            val totalSpent = categorySpending.sumOf { it.totalAmount }

            runOnUiThread {
                binding.tvReportTotal.text = "Total: ${totalSpent.toCurrencyString()}"

                // Category breakdown list
                val breakdown = StringBuilder()
                categorySpending.forEach { cs ->
                    val pct = if (totalSpent > 0) (cs.totalAmount / totalSpent * 100).toInt() else 0
                    breakdown.append("${cs.categoryName ?: "Uncategorized"}: ${cs.totalAmount.toCurrencyString()} ($pct%)\n")
                }
                binding.tvCategoryBreakdown.text = if (breakdown.isNotEmpty()) breakdown.toString() else "No data"

                // Pie chart
                setupPieChart(categorySpending.map { Pair(it.categoryName ?: "Uncategorized", it.totalAmount.toFloat()) })

                // Bar chart
                setupBarChart(categorySpending.map { Pair(it.categoryName ?: "Uncategorized", it.totalAmount.toFloat()) })
            }
        }
    }

    private fun setupPieChart(data: List<Pair<String, Float>>) {
        val entries = data.map { PieEntry(it.second, it.first) }
        if (entries.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            return
        }
        binding.pieChart.visibility = View.VISIBLE

        val dataSet = PieDataSet(entries, "Spending by Category")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f

        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData
        binding.pieChart.description.isEnabled = false
        binding.pieChart.centerText = "Categories"
        binding.pieChart.setCenterTextSize(14f)
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()
    }

    private fun setupBarChart(data: List<Pair<String, Float>>) {
        val entries = data.mapIndexed { idx, pair -> BarEntry(idx.toFloat(), pair.second) }
        if (entries.isEmpty()) {
            binding.barChart.visibility = View.GONE
            return
        }
        binding.barChart.visibility = View.VISIBLE

        val dataSet = BarDataSet(entries, "Amount Spent")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        binding.barChart.data = barData
        binding.barChart.description.isEnabled = false
        binding.barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.first })
        binding.barChart.xAxis.granularity = 1f
        binding.barChart.xAxis.labelRotationAngle = -30f
        binding.barChart.animateY(1000)
        binding.barChart.invalidate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
