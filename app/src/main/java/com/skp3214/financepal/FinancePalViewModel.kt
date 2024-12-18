package com.skp3214.financepal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FinanceViewModel(private val repository: FinancePalRepository) : ViewModel() {

    fun addEntry(entry: Model) {
        viewModelScope.launch {
            repository.addEntry(entry)
        }
    }

    fun getAllEntries(callback: (List<Model>) -> Unit) {
        viewModelScope.launch {
            val entries = repository.getAllEntries()
            callback(entries)
        }
    }

    fun getEntriesByCategory(category: String, callback: (List<Model>) -> Unit) {
        viewModelScope.launch {
            val entries = repository.getEntriesByCategory(category)
            callback(entries)
        }
    }

    fun deleteEntry(entry: Model) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun updateEntry(entry: Model) {
        viewModelScope.launch {
            repository.updateEntry(entry)
        }
    }
}
