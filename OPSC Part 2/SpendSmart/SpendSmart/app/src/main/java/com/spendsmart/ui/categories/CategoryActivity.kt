package com.spendsmart.ui.categories

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.spendsmart.R
import com.spendsmart.data.database.SpendSmartDatabase
import com.spendsmart.data.entities.Category
import com.spendsmart.databinding.ActivityCategoryBinding
import com.spendsmart.databinding.ItemCategoryBinding
import com.spendsmart.utils.SessionManager
import com.spendsmart.utils.showToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: SpendSmartDatabase

    private val colorOptions = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    )
    private var selectedColor = "#4CAF50"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = SpendSmartDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Categories"

        setupRecyclerView()
        loadCategories()

        binding.fabAddCategory.setOnClickListener { showAddCategoryDialog() }
    }

    private fun setupRecyclerView() {
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
    }

    private fun loadCategories() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            db.categoryDao().getCategoriesByUser(userId).collectLatest { categories ->
                val adapter = CategoryAdapter(
                    categories = categories,
                    onEdit = { showEditCategoryDialog(it) },
                    onDelete = { deleteCategory(it) }
                )
                runOnUiThread {
                    binding.rvCategories.adapter = adapter
                    binding.tvEmptyState.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showAddCategoryDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_category, null)
        val etName = view.findViewById<EditText>(R.id.etCategoryName)

        AlertDialog.Builder(this)
            .setTitle("Add Category")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    showToast("Category name cannot be empty")
                    return@setPositiveButton
                }
                val category = Category(
                    userId = sessionManager.getUserId(),
                    name = name,
                    colorHex = selectedColor
                )
                lifecycleScope.launch {
                    db.categoryDao().insertCategory(category)
                    runOnUiThread { showToast("Category added!") }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditCategoryDialog(category: Category) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_category, null)
        val etName = view.findViewById<EditText>(R.id.etCategoryName)
        etName.setText(category.name)

        AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) { showToast("Name cannot be empty"); return@setPositiveButton }
                lifecycleScope.launch {
                    db.categoryDao().updateCategory(category.copy(name = name))
                    runOnUiThread { showToast("Category updated!") }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCategory(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Delete '${category.name}'? Expenses won't be deleted.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    db.categoryDao().deleteCategory(category)
                    runOnUiThread { showToast("Category deleted") }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}

class CategoryAdapter(
    private val categories: List<Category>,
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    inner class VH(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cat = categories[position]
        holder.binding.tvCategoryName.text = cat.name
        try {
            holder.binding.viewColor.setBackgroundColor(Color.parseColor(cat.colorHex))
        } catch (e: Exception) {
            holder.binding.viewColor.setBackgroundColor(Color.GRAY)
        }
        holder.binding.btnEditCategory.setOnClickListener { onEdit(cat) }
        holder.binding.btnDeleteCategory.setOnClickListener { onDelete(cat) }
    }

    override fun getItemCount() = categories.size
}
