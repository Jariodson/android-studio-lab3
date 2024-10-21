package com.lab1.lab3

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MyListAdapter(
    context: Activity,
    private val dataSource: MutableList<MainActivity.ListItem>,
    private val coroutineScope: CoroutineScope,
    private val database: MainActivity.AppDatabase
) : ArrayAdapter<MainActivity.ListItem>(context, R.layout.list_item) {

    init {
        // Проверка количества элементов в dataSource
        Log.d("MyListAdapter", "Количество элементов в адаптере: ${dataSource.size}")
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.list_item,
            parent,
            false
        )

        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val deleteButton = view.findViewById<Button>(R.id.delete_button)

        val item = dataSource[position]
        checkBox.text = item.task
        checkBox.isChecked = item.status

        deleteButton.setOnClickListener {
            coroutineScope.launch {
                try {
                    database.listItemDao().delete(item)
                    dataSource.remove(item)
                    notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e("MyListAdapter", "Ошибка при удалении элемента", e)
                    Toast.makeText(context, "Ошибка при удалении элемента", Toast.LENGTH_SHORT).show()
                }
            }
        }

        checkBox.setOnCheckedChangeListener(null) // Отключаем слушатель
        checkBox.isChecked = item.status // Устанавливаем состояние
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            coroutineScope.launch {
                item.status = isChecked
                database.listItemDao().update(item)
            }
        }
        return view
    }
}