package com.section11.expenselens.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.EXPENSES_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.HOUSEHOLDS_COLLECTION
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.data.dto.FirestoreHousehold
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.ExpensesRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreExpensesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
): ExpensesRepository {

    override suspend fun createHousehold(
        userId: String,
        householdName: String
    ): Result<UserHousehold> {
        return try {
            val householdId = firestore.collection(HOUSEHOLDS_COLLECTION).document().id
            val users = listOf(userId)

            val household = FirestoreHousehold(
                id = householdId,
                name = householdName,
                users = users
            )

            firestore.collection(HOUSEHOLDS_COLLECTION)
                .document(householdId)
                .set(household)
                .await()

            Result.success(UserHousehold(householdId, householdName))
        } catch (exception: FirebaseFirestoreException) {
            Log.e(FirebaseFirestoreException::class.simpleName, exception.stackTraceToString())
            Result.failure(exception)
        }
    }

    override suspend fun deleteHousehold(userId: String, householdId: String): Result<Unit> {
        return try {
            firestore.collection(HOUSEHOLDS_COLLECTION)
                .document(householdId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }

    override suspend fun addExpenseToHousehold(
        userData: UserData,
        householdId: String,
        expense: ConsolidatedExpenseInformation
    ): Result<Unit> {
        val firestoreExpense = FirestoreExpense(
            total = expense.total,
            category = expense.category.displayName,
            date = Timestamp(expense.date),
            userId = userData.id,
            userDisplayName = userData.displayName,
            note = expense.note,
            distributedExpense = expense.distributedExpense
        )
        return try {
            firestore.collection(HOUSEHOLDS_COLLECTION)
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
            val expensesCollection = firestore.collection(HOUSEHOLDS_COLLECTION)
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
