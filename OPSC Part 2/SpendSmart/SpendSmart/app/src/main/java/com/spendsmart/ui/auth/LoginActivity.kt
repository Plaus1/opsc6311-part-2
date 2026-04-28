package com.spendsmart.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.databinding.ActivityLoginBinding
import com.spendsmart.ui.dashboard.MainActivity
import com.spendsmart.utils.SessionManager
import com.spendsmart.utils.showToast
import com.spendsmart.utils.toMD5Hash
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: SpendSmartDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = SpendSmartDatabase.getInstance(this)

        if (sessionManager.isLoggedIn()) {
            startMainActivity()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (!validateInputs(username, password)) return@setOnClickListener

            lifecycleScope.launch {
                val passwordHash = password.toMD5Hash()
                val user = db.userDao().login(username, passwordHash)
                if (user != null) {
                    sessionManager.saveSession(user.id, user.username)
                    startMainActivity()
                } else {
                    runOnUiThread {
                        showToast("Invalid username or password")
                    }
                }
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(username: String, password: String): Boolean {
        var isValid = true
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            isValid = false
        } else {
            binding.tilUsername.error = null
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        return isValid
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
