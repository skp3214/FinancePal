package com.skp3214.financepal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skp3214.financepal.model.Model
import com.skp3214.financepal.repository.FinancePalRepository
import kotlinx.coroutines.launch

class FinanceViewModel(private val repository: FinancePalRepository) : ViewModel() {

    private val categoryLiveDataMap = mutableMapOf<String, LiveData<MutableList<Model>>>()

    val allEntryItems: LiveData<MutableList<Model>> = repository.allEntries

    fun getEntriesByCategory(category: String): LiveData<MutableList<Model>> {
        return categoryLiveDataMap.getOrPut(category) {
            repository.getEntriesByCategory(category)
        }
    }

    fun addEntry(entry: Model) = viewModelScope.launch {
        repository.addEntry(entry)
    }

    fun deleteEntry(entry: Model) = viewModelScope.launch {
        repository.deleteEntry(entry)
    }

    fun updateEntry(entry: Model) = viewModelScope.launch {
        repository.updateEntry(entry)
    }

    class FinanceViewModelFactory(private val repository: FinancePalRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FinanceViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

