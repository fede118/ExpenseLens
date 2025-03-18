package com.section11.expenselens.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.HOUSEHOLDS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.HouseholdsCollection.EXPENSES_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.HouseholdsCollection.ExpensesArray.TIMESTAMP_FIELD
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.data.dto.FirestoreHousehold
import com.section11.expenselens.data.mapper.toDomainExpense
import com.section11.expenselens.data.mapper.toHouseholdDetails
import com.section11.expenselens.domain.exceptions.HouseholdNotFoundException
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.models.HouseholdDetails
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdRepository
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class FirestoreHouseholdRepository @Inject constructor(
    private val firestore: FirebaseFirestore
): HouseholdRepository {

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
            timestamp = Timestamp(expense.date),
            userId = userData.id,
            userDisplayName = userData.displayName,
            note = expense.note,
            distributedExpense = expense.distributedExpense
        )
        return try {
            firestore.collection(HOUSEHOLDS_COLLECTION)
                .document(householdId)
                .collection(EXPENSES_FIELD)
                .add(firestoreExpense)
                .await()
            Result.success(Unit)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }

    override suspend fun deleteExpenseFromHousehold(
        householdId: String,
        expenseId: String
    ): Result<Unit> {
        return try {
            firestore.collection(HOUSEHOLDS_COLLECTION)
                .document(householdId)
                .collection(EXPENSES_FIELD)
                .document(expenseId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }

    override suspend fun getAllExpensesFromHousehold(
        householdId: String
    ): Result<List<Expense>> {
        return try {
            val expensesCollection = firestore.collection(HOUSEHOLDS_COLLECTION)
                .document(householdId)
                .collection(EXPENSES_FIELD)

            val querySnapshot = expensesCollection.get().await()

            val expenses = mutableListOf<Expense>()
            for (document in querySnapshot.documents) {
                val expense = document.toObject(FirestoreExpense::class.java)
                if (expense != null) {
                    expenses.add(expense.toDomainExpense(document.id))
                }
            }
            Result.success(expenses)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }

    override suspend fun getExpensesForTimePeriod(
        householdId: String,
        firstDayOfCurrentMonth: Date,
        lastDayOfCurrentMonth: Date
    ): Result<List<Expense>> {
        return try {
            val expensesQuery = firestore.collection(HOUSEHOLDS_COLLECTION)
                .document(householdId)
                .collection(EXPENSES_FIELD)
                .whereGreaterThanOrEqualTo(TIMESTAMP_FIELD, Timestamp(firstDayOfCurrentMonth))
                .whereLessThanOrEqualTo(TIMESTAMP_FIELD, Timestamp(lastDayOfCurrentMonth))
                .get()
                .await()

            val expenses = mutableListOf<Expense>()
            for (document in expensesQuery.documents) {
                val expense = document.toObject(FirestoreExpense::class.java)
                if (expense != null) {
                    expenses.add(expense.toDomainExpense(document.id))
                }
            }
            Result.success(expenses)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }

    override suspend fun getHouseholdDetails(householdId: String): Result<HouseholdDetails> {
        return try {
            val householdDoc = firestore.collection(HOUSEHOLDS_COLLECTION)
                .document(householdId)
                .get()
                .await()

            val householdDetails = householdDoc
                .toObject(FirestoreHousehold::class.java)
                ?.toHouseholdDetails()

            if (householdDetails == null) {
                Result.failure(HouseholdNotFoundException())
            } else {
                Result.success(householdDetails)
            }
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }
}
