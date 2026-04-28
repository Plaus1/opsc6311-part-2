package com.spendsmart.ui.goals

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.data.entities.BudgetGoal
import com.spendsmart.databinding.ActivityGoalsBinding
import com.spendsmart.utils.*
import kotlinx.coroutines.launch
import java.util.Calendar

class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: SpendSmartDatabase

    private var currentMonth = getCurrentMonth()
    private var currentYear = getCurrentYear()
    private var existingGoal: BudgetGoal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = SpendSmartDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Budget Goals"

        val cal = Calendar.getInstance()
        val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault())
        binding.tvCurrentMonth.text = "$monthName $currentYear"

        loadCurrentGoal()
        setupSeekBars()
        setupSaveButton()
    }

    private fun loadCurrentGoal() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            existingGoal = db.budgetGoalDao().getGoalForMonth(userId, currentMonth, currentYear)
            val totalSpent = db.expenseDao().getTotalSpending(
                userId,
                getStartOfMonth(currentMonth, currentYear),
                getEndOfMonth(currentMonth, currentYear)
            ) ?: 0.0

            runOnUiThread {
                existingGoal?.let { goal ->
                    binding.etMinGoal.setText(goal.minGoal.toString())
                    binding.etMaxGoal.setText(goal.maxGoal.toString())

                    val minProgress = ((totalSpent / goal.minGoal) * 100).toInt().coerceIn(0, 100)
                    val maxProgress = ((totalSpent / goal.maxGoal) * 100).toInt().coerceIn(0, 100)
                    binding.progressMinGoal.progress = minProgress
                    binding.progressMaxGoal.progress = maxProgress
                }
                binding.tvCurrentSpending.text = "Current spending: ${totalSpent.toCurrencyString()}"
            }
        }
    }

    private fun setupSeekBars() {
        // SeekBar for min goal (100 to 50000 in steps)
        binding.seekBarMinGoal.max = 500
        binding.seekBarMinGoal.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val value = (progress * 100).coerceAtLeast(100)
                    binding.etMinGoal.setText(value.toString())
                }
            }
            override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
        })

        binding.seekBarMaxGoal.max = 500
        binding.seekBarMaxGoal.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val value = (progress * 100).coerceAtLeast(100)
                    binding.etMaxGoal.setText(value.toString())
                }
            }
            override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
        })
    }

    private fun setupSaveButton() {
        binding.btnSaveGoal.setOnClickListener {
            val minStr = binding.etMinGoal.text.toString().trim()
            val maxStr = binding.etMaxGoal.text.toString().trim()

            if (minStr.isEmpty()) {
                binding.tilMinGoal.error = "Minimum goal is required"
                return@setOnClickListener
            }
            binding.tilMinGoal.error = null

            if (maxStr.isEmpty()) {
                binding.tilMaxGoal.error = "Maximum goal is required"
                return@setOnClickListener
            }
            binding.tilMaxGoal.error = null

            val min = minStr.toDoubleOrNull()
            val max = maxStr.toDoubleOrNull()

            if (min == null || min <= 0) {
                binding.tilMinGoal.error = "Enter a valid minimum"
                return@setOnClickListener
            }
            if (max == null || max <= 0) {
                binding.tilMaxGoal.error = "Enter a valid maximum"
                return@setOnClickListener
            }
            if (min > max) {
                binding.tilMinGoal.error = "Minimum cannot exceed maximum"
                return@setOnClickListener
            }

            val userId = sessionManager.getUserId()
            lifecycleScope.launch {
                val goal = existingGoal?.copy(minGoal = min, maxGoal = max)
                    ?: BudgetGoal(userId = userId, minGoal = min, maxGoal = max, month = currentMonth, year = currentYear)
                db.budgetGoalDao().insertGoal(goal)
                runOnUiThread {
                    showToast("Budget goal saved!")
                    finish()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
