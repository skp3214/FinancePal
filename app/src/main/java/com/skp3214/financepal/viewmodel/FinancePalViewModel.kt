package com.skp3214.financepal.viewmodel

import androidx.lifecycle.*
import com.skp3214.financepal.model.Model
import com.skp3214.financepal.repository.FinancePalRepository
import kotlinx.coroutines.launch

class FinanceViewModel(private val repository: FinancePalRepository) : ViewModel() {

    private val _allEntryItems = MutableLiveData<List<Model>>()
    val allEntryItems: LiveData<List<Model>> get() = _allEntryItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        _isLoading.value = true
        observeAllEntries()
    }

    private fun observeAllEntries() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _allEntryItems.value = entries
                _isLoading.value = false
            }
        }
    }

    fun getEntriesByCategory(category: String): LiveData<List<Model>> {
        val filteredLiveData = MutableLiveData<List<Model>>()
        _allEntryItems.observeForever { allEntries ->
            val filteredEntries = allEntries?.filter { it.category == category } ?: emptyList()
            filteredLiveData.value = filteredEntries
        }
        return filteredLiveData
    }

    fun addEntry(entry: Model) {
        repository.addEntry(entry) { success ->
            // Don't set loading to false here - let the flow update handle it
        }
    }

    fun deleteEntry(entry: Model) {
        repository.deleteEntry(entry) { success ->
            // Don't set loading to false here - let the flow update handle it
        }
    }

    fun updateEntry(entry: Model) {
        repository.updateEntry(entry) { success ->
            // Don't set loading to false here - let the flow update handle it
        }
    }

    fun getEntryById(id: String): LiveData<Model?> {
        val entryLiveData = MutableLiveData<Model?>()
        _allEntryItems.observeForever { allEntries ->
            val entry = allEntries?.find { it.id == id }
            entryLiveData.value = entry
        }
        return entryLiveData
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
