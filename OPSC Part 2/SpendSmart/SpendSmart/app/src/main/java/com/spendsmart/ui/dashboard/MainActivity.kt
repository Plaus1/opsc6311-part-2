package com.spendsmart.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.spendsmart.R
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.databinding.ActivityMainBinding
import com.spendsmart.ui.achievements.AchievementsActivity
import com.spendsmart.ui.auth.LoginActivity
import com.spendsmart.ui.categories.CategoryActivity
import com.spendsmart.ui.expenses.AddExpenseActivity
import com.spendsmart.ui.expenses.ExpenseListActivity
import com.spendsmart.ui.goals.GoalsActivity
import com.spendsmart.ui.reports.ReportsActivity
import com.spendsmart.utils.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: SpendSmartDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = SpendSmartDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)

        setupUI()
        setupClickListeners()
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun setupUI() {
        binding.tvWelcome.text = "Welcome, ${sessionManager.getUsername()}!"
        binding.tvStreak.text = "🔥 ${sessionManager.getStreak()} day streak"
    }

    private fun setupClickListeners() {
        binding.fabAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        binding.cardAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        binding.cardViewExpenses.setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }
        binding.cardCategories.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }
        binding.cardGoals.setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }
        binding.cardReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        binding.cardAchievements.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }
    }

    private fun loadDashboardData() {
        val userId = sessionManager.getUserId()
        val currentMonth = getCurrentMonth()
        val currentYear = getCurrentYear()
        val startDate = getStartOfMonth(currentMonth, currentYear)
        val endDate = getEndOfMonth(currentMonth, currentYear)

        lifecycleScope.launch {
            val totalSpent = db.expenseDao().getTotalSpending(userId, startDate, endDate) ?: 0.0
            val goal = db.budgetGoalDao().getGoalForMonth(userId, currentMonth, currentYear)

            runOnUiThread {
                binding.tvTotalSpent.text = totalSpent.toCurrencyString()

                val cal = Calendar.getInstance()
                val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault())
                binding.tvMonthLabel.text = "$monthName ${currentYear}"

                if (goal != null) {
                    val maxGoal = goal.maxGoal
                    val progress = if (maxGoal > 0) ((totalSpent / maxGoal) * 100).toInt().coerceIn(0, 100) else 0
                    binding.progressBudget.progress = progress
                    binding.tvBudgetStatus.text = "${totalSpent.toCurrencyString()} of ${maxGoal.toCurrencyString()}"

                    when {
                        totalSpent > maxGoal -> {
                            binding.tvBudgetStatus.setTextColor(getColor(R.color.red_error))
                            binding.tvBudgetWarning.text = "⚠️ Over budget!"
                            binding.tvBudgetWarning.visibility = android.view.View.VISIBLE
                        }
                        totalSpent >= maxGoal * 0.8 -> {
                            binding.tvBudgetStatus.setTextColor(getColor(R.color.orange_warning))
                            binding.tvBudgetWarning.text = "⚠️ Nearing budget limit"
                            binding.tvBudgetWarning.visibility = android.view.View.VISIBLE
                        }
                        else -> {
                            binding.tvBudgetStatus.setTextColor(getColor(R.color.green_primary))
                            binding.tvBudgetWarning.visibility = android.view.View.GONE
                        }
                    }
                } else {
                    binding.progressBudget.progress = 0
                    binding.tvBudgetStatus.text = "No budget set for this month"
                    binding.tvBudgetWarning.visibility = android.view.View.GONE
                }

                // Update streak
                binding.tvStreak.text = "🔥 ${sessionManager.getStreak()} day streak"
                binding.tvBadges.text = "🏅 ${sessionManager.getTotalBadges()} badges earned"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
