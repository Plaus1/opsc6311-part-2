package com.spendsmart.ui.achievements

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.databinding.ActivityAchievementsBinding
import com.spendsmart.utils.*
import kotlinx.coroutines.launch

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: SpendSmartDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = SpendSmartDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Achievements"

        loadAchievements()
    }

    private fun loadAchievements() {
        val userId = sessionManager.getUserId()
        val streak = sessionManager.getStreak()
        val badges = sessionManager.getTotalBadges()

        lifecycleScope.launch {
            val expenseCount = db.expenseDao().getExpenseCount(userId)
            val catCount = db.categoryDao().getCategoryCount(userId)

            runOnUiThread {
                // Streak badge
                binding.tvStreakBadge.text = if (streak >= 7) "🏅 7-Day Streak Achieved!" else "🔒 7-Day Streak (${streak}/7 days)"
                binding.tvMonthStreakBadge.text = if (streak >= 30) "🥇 30-Day Streak Achieved!" else "🔒 30-Day Streak (${streak}/30 days)"

                // Expense milestones
                binding.tvFirstExpense.text = if (expenseCount >= 1) "✅ First Expense Logged!" else "🔒 Log your first expense"
                binding.tvTenExpenses.text = if (expenseCount >= 10) "✅ 10 Expenses Logged!" else "🔒 Log 10 expenses (${expenseCount}/10)"
                binding.tvFiftyExpenses.text = if (expenseCount >= 50) "✅ 50 Expenses Logged!" else "🔒 Log 50 expenses (${expenseCount}/50)"

                // Category badge
                binding.tvCategoryBadge.text = if (catCount >= 5) "✅ Budget Organizer – 5+ Categories!" else "🔒 Create 5 categories (${catCount}/5)"

                // Summary
                binding.tvTotalBadges.text = "Total Badges Earned: $badges 🏅"
                binding.tvCurrentStreak.text = "Current Streak: $streak 🔥"

                // Progress bars
                binding.progressStreak7.progress = (streak * 100 / 7).coerceIn(0, 100)
                binding.progressStreak30.progress = (streak * 100 / 30).coerceIn(0, 100)
                binding.progressExpenses.progress = (expenseCount * 100 / 50).coerceIn(0, 100)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
