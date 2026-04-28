package com.spendsmart.ui.expenses

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.spendsmart.R
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.data.entities.Category
import com.spendsmart.data.entities.Expense
import com.spendsmart.databinding.ActivityAddExpenseBinding
import com.spendsmart.utils.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: SpendSmartDatabase

    private var categories: List<Category> = emptyList()
    private var selectedDate: Long = System.currentTimeMillis()
    private var photoUri: Uri? = null
    private var photoPath: String? = null
    private var startTime: String = ""
    private var endTime: String = ""
    private var editExpenseId: Long = -1L

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            binding.ivReceipt.visibility = View.VISIBLE
            Glide.with(this).load(photoUri).into(binding.ivReceipt)
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            photoUri = it
            photoPath = it.toString()
            binding.ivReceipt.visibility = View.VISIBLE
            Glide.with(this).load(it).into(binding.ivReceipt)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = SpendSmartDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editExpenseId = intent.getLongExtra("expense_id", -1L)
        if (editExpenseId != -1L) {
            supportActionBar?.title = "Edit Expense"
            loadExpenseForEdit()
        } else {
            supportActionBar?.title = "Add Expense"
        }

        updateDateDisplay()
        loadCategories()
        setupClickListeners()
    }

    private fun loadExpenseForEdit() {
        lifecycleScope.launch {
            val expense = db.expenseDao().getExpenseById(editExpenseId) ?: return@launch
            runOnUiThread {
                binding.etAmount.setText(expense.amount.toString())
                binding.etDescription.setText(expense.description)
                selectedDate = expense.date
                startTime = expense.startTime
                endTime = expense.endTime
                updateDateDisplay()
                binding.tvStartTime.text = if (startTime.isNotEmpty()) startTime else "Set Start Time"
                binding.tvEndTime.text = if (endTime.isNotEmpty()) endTime else "Set End Time"

                expense.photoPath?.let { path ->
                    photoPath = path
                    binding.ivReceipt.visibility = View.VISIBLE
                    Glide.with(this@AddExpenseActivity).load(path).into(binding.ivReceipt)
                }
            }
        }
    }

    private fun loadCategories() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            categories = db.categoryDao().getCategoriesByUser(userId).first()
            runOnUiThread {
                val categoryNames = categories.map { it.name }
                val adapter = ArrayAdapter(this@AddExpenseActivity, android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategory.adapter = adapter

                if (editExpenseId != -1L) {
                    lifecycleScope.launch {
                        val expense = db.expenseDao().getExpenseById(editExpenseId) ?: return@launch
                        val idx = categories.indexOfFirst { it.id == expense.categoryId }
                        if (idx >= 0) runOnUiThread { binding.spinnerCategory.setSelection(idx) }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnStartTime.setOnClickListener { showTimePicker(true) }
        binding.btnEndTime.setOnClickListener { showTimePicker(false) }
        binding.btnTakePhoto.setOnClickListener { takePhoto() }
        binding.btnPickPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnRemovePhoto.setOnClickListener {
            photoUri = null
            photoPath = null
            binding.ivReceipt.visibility = View.GONE
        }
        binding.btnSaveExpense.setOnClickListener { saveExpense() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDate
        DatePickerDialog(this, { _, year, month, day ->
            val newCal = Calendar.getInstance()
            newCal.set(year, month, day, 12, 0, 0)
            selectedDate = newCal.timeInMillis
            updateDateDisplay()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            val timeStr = String.format("%02d:%02d", hour, minute)
            if (isStart) {
                startTime = timeStr
                binding.tvStartTime.text = timeStr
            } else {
                endTime = timeStr
                binding.tvEndTime.text = timeStr
            }
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun takePhoto() {
        val photoFile = createImageFile()
        photoFile?.also {
            photoPath = it.absolutePath
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
            photoUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (e: Exception) {
            null
        }
    }

    private fun updateDateDisplay() {
        binding.tvSelectedDate.text = selectedDate.toFormattedDate()
    }

    private fun saveExpense() {
        val amountStr = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (amountStr.isEmpty()) {
            binding.tilAmount.error = "Amount is required"
            return
        }
        binding.tilAmount.error = null

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.tilAmount.error = "Please enter a valid amount"
            return
        }

        if (description.isEmpty()) {
            binding.tilDescription.error = "Description is required"
            return
        }
        binding.tilDescription.error = null

        val selectedCategoryIndex = binding.spinnerCategory.selectedItemPosition
        val categoryId = if (categories.isNotEmpty() && selectedCategoryIndex >= 0) {
            categories[selectedCategoryIndex].id
        } else null

        val userId = sessionManager.getUserId()

        lifecycleScope.launch {
            if (editExpenseId != -1L) {
                val existing = db.expenseDao().getExpenseById(editExpenseId)
                existing?.let {
                    db.expenseDao().updateExpense(
                        it.copy(
                            amount = amount,
                            description = description,
                            date = selectedDate,
                            startTime = startTime,
                            endTime = endTime,
                            categoryId = categoryId,
                            photoPath = photoPath
                        )
                    )
                }
            } else {
                val expense = Expense(
                    userId = userId,
                    categoryId = categoryId,
                    amount = amount,
                    description = description,
                    date = selectedDate,
                    startTime = startTime,
                    endTime = endTime,
                    photoPath = photoPath
                )
                db.expenseDao().insertExpense(expense)

                // Update streak
                updateStreak()
            }

            runOnUiThread {
                showToast("Expense saved successfully!")
                finish()
            }
        }
    }

    private fun updateStreak() {
        val today = getStartOfDay(System.currentTimeMillis())
        val lastLog = sessionManager.getLastLogDate()
        val yesterday = today - 86_400_000L

        when {
            lastLog == today -> { /* already logged today */ }
            lastLog == yesterday -> {
                sessionManager.setStreak(sessionManager.getStreak() + 1)
                sessionManager.setLastLogDate(today)
            }
            else -> {
                sessionManager.setStreak(1)
                sessionManager.setLastLogDate(today)
            }
        }

        val streak = sessionManager.getStreak()
        var badges = sessionManager.getTotalBadges()
        if (streak == 7 || streak == 30) badges++
        sessionManager.setTotalBadges(badges)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
