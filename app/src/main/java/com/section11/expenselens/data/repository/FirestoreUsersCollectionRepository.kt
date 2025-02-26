package com.section11.expenselens.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.EMAIL_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.HOUSEHOLDS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.INVITATIONS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_ID_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_NAME_FIELD
import com.section11.expenselens.data.mapper.mapToHouseholdsList
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUsersCollectionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UsersCollectionRepository {

    override suspend fun createUserIfNotExists(userId: String, email: String): Result<Unit> {
        return try {
            val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)
            val snapshot = userDocRef.get().await()

            if (!snapshot.exists()) {
                val userData = mapOf(
                    EMAIL_FIELD to email,
                    HOUSEHOLDS_FIELD to emptyList<Map<String, Any>>(), // Initialize empty household list
                    INVITATIONS_FIELD to emptyList<Map<String, Any>>() // Initialize empty invitation list
                )
                userDocRef.set(userData).await()
            }
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    override suspend fun getUserHouseholds(userId: String): List<UserHousehold> {
        val userDoc = firestore.collection(USERS_COLLECTION).document(userId).get().await()
        val households = (userDoc.get(HOUSEHOLDS_FIELD) as? List<*>)?.mapToHouseholdsList()

        return households ?: emptyList()
    }

    override suspend fun addHouseholdToUser(
        userId: String,
        household: UserHousehold
    ): Result<Unit> {
        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)
        return try {
            firestore.runTransaction { transaction ->
                val householdMap = mapOf(
                    HOUSEHOLD_ID_FIELD to household.id,
                    HOUSEHOLD_NAME_FIELD to household.name
                )
                transaction.update(userDocRef, HOUSEHOLDS_FIELD, FieldValue.arrayUnion(householdMap))
            }.await()

            Result.success(Unit)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }
}
