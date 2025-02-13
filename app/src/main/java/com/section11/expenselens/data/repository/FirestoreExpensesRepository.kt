package com.section11.expenselens.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.data.dto.FirestoreHousehold
import com.section11.expenselens.data.dto.FirestoreHousehold.Companion.NAME_FIELD
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.repository.ExpensesRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val HOUSEHOLD_COLLECTION = "households"
private const val EXPENSES_COLLECTION = "expenses"

class FirestoreExpensesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
): ExpensesRepository {

    override suspend fun getHousehold(householdName: String): String? {
        return try {
            val householdsCollection = firestore.collection(HOUSEHOLD_COLLECTION)
            val querySnapshot = householdsCollection
                .whereEqualTo(NAME_FIELD, householdName)
                .get().await()

            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.firstOrNull()?.id // Return the ID of the first matching document
            } else {
                null
            }
        } catch (e: FirebaseFirestoreException) {
            when(e.code) {
                NOT_FOUND -> null
                else -> throw e
            }
        }
    }

    override suspend fun createHousehold(
        householdName: String,
        userId: String
    ): Result<Pair<String, String>> {
        return try {
            val householdId = firestore.collection(HOUSEHOLD_COLLECTION).document().id
            val users = listOf(userId)

            val household = FirestoreHousehold(
                id = householdId,
                name = householdName,
                users = users
            )

            firestore.collection(HOUSEHOLD_COLLECTION)
                .document(householdId)
                .set(household)
                .await()

            Result.success(householdId to householdName)
        } catch (exception: FirebaseFirestoreException) {
            Log.e("FirebaseFirestoreException", exception.stackTraceToString())
            Result.failure(exception)
        }
    }

    override suspend fun addExpenseToHousehold(
        userId: String,
        householdId: String,
        expense: ConsolidatedExpenseInformation
    ): Result<Unit> {
        val firestoreExpense = FirestoreExpense(
            total = expense.total,
            category = expense.category.displayName,
            date = Timestamp(expense.date),
            userId = userId,
            note = expense.note ?: String(),
            distributedExpense = expense.distributedExpense
        )
        return try {
            firestore.collection(HOUSEHOLD_COLLECTION)
                .document(householdId)
                .collection(EXPENSES_COLLECTION)
                .add(firestoreExpense)
                .await()
            Result.success(Unit)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }

    override suspend fun getAllExpensesFromHousehold(
        householdId: String
    ): Result<List<FirestoreExpense>> {
        return try {
            val expensesCollection = firestore.collection(HOUSEHOLD_COLLECTION)
                .document(householdId)
                .collection(EXPENSES_COLLECTION)

            val querySnapshot = expensesCollection.get().await()

            val expenses = mutableListOf<FirestoreExpense>()
            for (document in querySnapshot.documents) {
                val expense = document.toObject(FirestoreExpense::class.java)
                if (expense != null) {
                    expenses.add(expense)
                }
            }
            Result.success(expenses)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }
}
