package com.skp3214.financepal.viewmodel

import androidx.lifecycle.*
import com.skp3214.financepal.model.Model
import com.skp3214.financepal.repository.FinancePalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FinanceViewModel(private val repository: FinancePalRepository) : ViewModel() {

    private val _allEntryItems = MutableLiveData<List<Model>>()
    val allEntryItems: LiveData<List<Model>> get() = _allEntryItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

//    private val categoryLiveDataMap = mutableMapOf<String, MutableLiveData<List<Model>>>()

    init {
        _isLoading.value = true
        fetchAllEntries()
    }

    private fun fetchAllEntries() = viewModelScope.launch {
        val entries = withContext(Dispatchers.IO) {
            repository.getAllEntries()
        }
        _allEntryItems.value = entries
        _isLoading.value = false
    }

    fun getEntriesByCategory(category: String): LiveData<List<Model>> {
        val filteredLiveData = MutableLiveData<List<Model>>()
        _isLoading.value = true
        viewModelScope.launch {
            val allEntries = _allEntryItems.value ?: emptyList()
            val filteredEntries = allEntries.filter { it.category == category }
            filteredLiveData.value = filteredEntries
            _isLoading.value = false
        }
        return filteredLiveData
    }

    fun addEntry(entry: Model) = viewModelScope.launch {
        _isLoading.value = true
        repository.addEntry(entry)
        fetchAllEntries()
        _isLoading.value = false
    }

    fun deleteEntry(entry: Model) = viewModelScope.launch {
        _isLoading.value = true
        repository.deleteEntry(entry)
        fetchAllEntries()
        _isLoading.value = false
    }

    fun updateEntry(entry: Model) = viewModelScope.launch {
        _isLoading.value = true
        repository.updateEntry(entry)
        fetchAllEntries()
        _isLoading.value = false
    }

    fun getEntryById(id: String): LiveData<Model?> {
        val entryLiveData = MutableLiveData<Model?>()
        _isLoading.value = true
        _allEntryItems.observeForever { allEntries ->
            val entry = allEntries.find { it.id == id }
            entryLiveData.value = entry
            _isLoading.value = false
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