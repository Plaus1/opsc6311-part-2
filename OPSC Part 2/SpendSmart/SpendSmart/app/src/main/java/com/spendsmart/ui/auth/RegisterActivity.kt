package com.spendsmart.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.data.entities.Category
import com.spendsmart.data.entities.User
import com.spendsmart.databinding.ActivityRegisterBinding
import com.spendsmart.ui.dashboard.MainActivity
import com.spendsmart.utils.SessionManager
import com.spendsmart.utils.showToast
import com.spendsmart.utils.toMD5Hash
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: SpendSmartDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = SpendSmartDatabase.getInstance(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (!validateInputs(username, password, confirmPassword)) return@setOnClickListener

            lifecycleScope.launch {
                val existingUser = db.userDao().getUserByUsername(username)
                if (existingUser != null) {
                    runOnUiThread {
                        binding.tilUsername.error = "Username already exists"
                    }
                    return@launch
                }

                val newUser = User(
                    username = username,
                    passwordHash = password.toMD5Hash()
                )
                val userId = db.userDao().insertUser(newUser)

                // Create default categories
                val defaultCategories = listOf(
                    Category(userId = userId, name = "Food & Dining", colorHex = "#FF5722", iconName = "ic_food"),
                    Category(userId = userId, name = "Transport", colorHex = "#2196F3", iconName = "ic_transport"),
                    Category(userId = userId, name = "Entertainment", colorHex = "#9C27B0", iconName = "ic_entertainment"),
                    Category(userId = userId, name = "Shopping", colorHex = "#FF9800", iconName = "ic_shopping"),
                    Category(userId = userId, name = "Healthcare", colorHex = "#F44336", iconName = "ic_health"),
                    Category(userId = userId, name = "Utilities", colorHex = "#607D8B", iconName = "ic_utilities"),
                    Category(userId = userId, name = "Other", colorHex = "#795548", iconName = "ic_other")
                )
                defaultCategories.forEach { db.categoryDao().insertCategory(it) }

                sessionManager.saveSession(userId, username)
                runOnUiThread {
                    showToast("Account created successfully!")
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finishAffinity()
                }
            }
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(username: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            isValid = false
        } else if (username.length < 3) {
            binding.tilUsername.error = "Username must be at least 3 characters"
            isValid = false
        } else {
            binding.tilUsername.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }
}
