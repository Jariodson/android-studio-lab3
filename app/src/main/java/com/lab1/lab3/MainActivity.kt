package com.lab1.lab3

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    private lateinit var listItems: MutableList<ListItem>
    private lateinit var adapter: MyListAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userInput = findViewById<EditText>(R.id.user_input)
        val addButton = findViewById<Button>(R.id.add_button)
        val listView = findViewById<ListView>(R.id.list_view)

        val database = (application as DataBaseInit).database
        val listItemDao = database.listItemDao()

        listItems = mutableListOf()
        adapter = MyListAdapter(this, listItems, lifecycleScope, database)
        listView.adapter = adapter

        lifecycleScope.launch {
            val items = listItemDao.getAllItems()
            if (items.isNotEmpty()) {
                listItems.addAll(items)
                Log.d("MainActivity", "Элементы списка: $listItems")
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this@MainActivity, "Список пуст", Toast.LENGTH_SHORT).show()
            }
        }

        addButton.setOnClickListener {
            val taskText = userInput.text.toString()
            if (taskText.isNotBlank()) {
                val newItem = ListItem(task = taskText, status = false)
                try {
                    lifecycleScope.launch {
                        listItemDao.insert(newItem)
                        listItems.add(newItem)
                        Log.d("MainActivity", "Элементы списка: $listItems")
                        adapter.notifyDataSetChanged()
                        userInput.setText("")
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка при добавлении задачи", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Может вы хотите что-то сделать?",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Entity(tableName = "to_do_list")
    data class ListItem(
        @PrimaryKey(autoGenerate = true) var id: Int? = null,
        var task: String,
        var status: Boolean
    )

    @Dao
    interface ListItemDao {

        @Update
        suspend fun update(listItem: ListItem)

        @Delete
        suspend fun delete(listItem: ListItem)

        @Insert
        suspend fun insert(listItem: ListItem)

        @Query("SELECT * FROM to_do_list")
        suspend fun getAllItems(): List<ListItem>
    }

    @Database(version = 1, entities = [ListItem::class])
    abstract class AppDatabase : RoomDatabase() {
        abstract fun listItemDao(): ListItemDao
    }
}

