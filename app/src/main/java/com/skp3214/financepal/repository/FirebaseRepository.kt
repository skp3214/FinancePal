package com.skp3214.financepal.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.skp3214.financepal.model.Model
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val transactionsCollection = db.collection("transactions")
    
    private val _localCache = MutableStateFlow<List<Model>>(emptyList())
    private var isInitialized = false

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        initializeCache()
    }

    private fun initializeCache() {
        val currentUser = auth.currentUser ?: return
        
        transactionsCollection
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                val serverModels = snapshot.documents.mapNotNull { document ->
                    Model(
                        id = document.getString("id") ?: "",
                        name = document.getString("name") ?: "",
                        amount = document.getDouble("amount") ?: 0.0,
                        description = document.getString("description") ?: "",
                        category = document.getString("category") ?: "",
                        image = document.getString("imageUrl") ?: "",
                        date = document.getString("date") ?: "",
                        dueDate = document.getString("dueDate") ?: "",
                        userId = document.getString("userId") ?: ""
                    )
                }
                
                if (!isInitialized) {
                    _localCache.value = serverModels
                    isInitialized = true
                } else {
                    // Merge server data with local pending changes
                    val currentCache = _localCache.value
                    val mergedList = mutableListOf<Model>()
                    
                    // Add all server items
                    mergedList.addAll(serverModels)
                    
                    // Add local items that aren't on server yet (pending adds)
                    currentCache.forEach { localItem ->
                        if (serverModels.none { it.id == localItem.id }) {
                            mergedList.add(localItem)
                        }
                    }
                    
                    _localCache.value = mergedList
                }
            }
    }

    fun getAllTransactionsFlow(): Flow<List<Model>> = _localCache.asStateFlow()

    fun addTransaction(model: Model, onResult: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: run { onResult(false); return }
        val docId = transactionsCollection.document().id
        val newModel = model.copy(id = docId, userId = currentUser.uid)
        
        // Immediately update local cache
        val currentList = _localCache.value.toMutableList()
        currentList.add(newModel)
        _localCache.value = currentList
        
        val transaction = hashMapOf(
            "id" to docId,
            "userId" to currentUser.uid,
            "name" to model.name,
            "amount" to model.amount,
            "description" to model.description,
            "category" to model.category,
            "imageUrl" to model.image,
            "date" to model.date,
            "dueDate" to model.dueDate
        )
        
        transactionsCollection.document(docId).set(transaction)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful || task.exception == null)
            }
    }

    fun updateTransaction(id: String, model: Model, onResult: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: run { onResult(false); return }
        
        // Immediately update local cache
        val currentList = _localCache.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        
        if (index >= 0) {
            currentList[index] = model
            _localCache.value = currentList
        }
        
        val updateData = hashMapOf(
            "id" to model.id,
            "userId" to currentUser.uid,
            "name" to model.name,
            "amount" to model.amount,
            "description" to model.description,
            "category" to model.category,
            "imageUrl" to model.image,
            "date" to model.date,
            "dueDate" to model.dueDate
        )
        
        transactionsCollection.document(id).update(updateData as Map<String, Any>)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful || task.exception == null)
            }
    }

    fun deleteTransaction(id: String, onResult: (Boolean) -> Unit) {
        auth.currentUser ?: run { onResult(false); return }
        
        // Immediately update local cache
        val currentList = _localCache.value.toMutableList()
        currentList.removeAll { it.id == id }
        _localCache.value = currentList
        
        transactionsCollection.document(id).delete()
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful || task.exception == null)
            }
    }

    suspend fun uploadImage(imageBytes: ByteArray): String {
        return try {
            val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
            val uploadTask = imageRef.putBytes(imageBytes).await()
            uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e // Let the caller handle the exception
        }
    }
}
