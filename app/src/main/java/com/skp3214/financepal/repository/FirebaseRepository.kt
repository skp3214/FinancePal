package com.skp3214.financepal.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skp3214.financepal.model.Model
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val transactionsCollection = db.collection("transactions")

    suspend fun addTransaction(model: Model) {
        try {
            val currentUser = auth.currentUser ?: return
            val docId = transactionsCollection.document().id
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
            transactionsCollection.document(docId).set(transaction).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAllTransactions(): List<Model> {
        return try {
            val currentUser = auth.currentUser ?: return emptyList()
            val snapshot = transactionsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get().await()

            snapshot.documents.mapNotNull { document ->
                val imageUrl = document.getString("imageUrl") ?: ""

                Model(
                    id = document.getString("id") ?: "",
                    name = document.getString("name") ?: "",
                    amount = document.getDouble("amount") ?: 0.0,
                    description = document.getString("description") ?: "",
                    category = document.getString("category") ?: "",
                    image = imageUrl,
                    date = document.getString("date") ?: "",
                    dueDate = document.getString("dueDate") ?: "",
                    userId = document.getString("userId") ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }



    suspend fun updateTransaction(id: String, model: Model): Boolean {
        try {
            val currentUser = auth.currentUser ?: return false
            val document = transactionsCollection.document(id).get().await()

            if (document.exists() && document.getString("userId") == currentUser.uid) {
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
                transactionsCollection.document(id).update(updateData.toMap()).await()
                return true
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun deleteTransaction(id: String): Boolean {
        try {
            val currentUser = auth.currentUser ?: return false
            val document = transactionsCollection.document(id).get().await()

            if (document.exists() && document.getString("userId") == currentUser.uid) {
                val imageUrl = document.getString("imageUrl")

                imageUrl?.let {
                    try {
                        storage.getReferenceFromUrl(it).delete().await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                transactionsCollection.document(id).delete().await()
                return true
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

//    suspend fun getTransactionsByCategory(category: String): List<Model> {
//        return try {
//            val currentUser = auth.currentUser ?: return emptyList()
//            val snapshot = transactionsCollection
//                .whereEqualTo("userId", currentUser.uid)
//                .whereEqualTo("category", category)
//                .get()
//                .await()
//            snapshot.documents.mapNotNull { document ->
//                val imageUrl = document.getString("imageUrl") ?: ""
//                val imageBytes = downloadImage(imageUrl)
//
//                Model(
//                    id = document.getString("id") ?: "",
//                    name = document.getString("name") ?: "",
//                    amount = document.getDouble("amount") ?: 0.0,
//                    description = document.getString("description") ?: "",
//                    category = document.getString("category") ?: "",
//                    image = imageBytes,
//                    date = document.getString("date") ?: "",
//                    dueDate = document.getString("dueDate") ?: "",
//                    userId = document.getString("userId") ?: ""
//                )
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emptyList()
//        }
//    }
//
//    suspend fun getTransaction(id: String): Model? {
//        try {
//            val currentUser = auth.currentUser ?: return null
//            val document = transactionsCollection.document(id).get().await()
//            if (document.exists() && document.getString("userId") == currentUser.uid) {
//                val imageUrl = document.getString("imageUrl") ?: ""
//                val imageBytes = downloadImage(imageUrl)
//
//                return Model(
//                    id = document.getString("id") ?: "",
//                    name = document.getString("name") ?: "",
//                    amount = document.getDouble("amount") ?: 0.0,
//                    description = document.getString("description") ?: "",
//                    category = document.getString("category") ?: "",
//                    image = imageBytes,
//                    date = document.getString("date") ?: "",
//                    dueDate = document.getString("dueDate") ?: "",
//                    userId = document.getString("userId") ?: ""
//                )
//            }
//            return null
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        }
//    }
    suspend fun uploadImage(imageBytes: ByteArray): String {
        val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
        return imageRef.putBytes(imageBytes).await().storage.downloadUrl.await().toString()
    }

}
