package com.section11.expenselens.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.EMAIL_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.HOUSEHOLDS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.NOTIFICATIONS_TOKEN_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_ID_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_NAME_FIELD
import com.section11.expenselens.data.mapper.mapToHouseholdsList
import com.section11.expenselens.domain.exceptions.UserNotFoundException
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUsersCollectionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UsersCollectionRepository {

    override suspend fun createOrUpdateUser(userData: UserData): Result<Unit> {
        return try {
            val userDocRef = firestore.collection(USERS_COLLECTION)
            val snapshot = userDocRef.document(userData.id).get().await()
            val userDataMap = hashMapOf(
                EMAIL_FIELD to userData.email,
                NOTIFICATIONS_TOKEN_FIELD to userData.notificationToken,
                HOUSEHOLDS_FIELD to emptyList<Map<String, Any>>(), // Initialize empty household list
            )
            if (snapshot.exists()) {
                (snapshot.get(HOUSEHOLDS_FIELD) as? List<Map<String, Any>>)?.let {
                    userDataMap[HOUSEHOLDS_FIELD] = it
                }
                userDocRef
                    .document(userData.id)
                    .update(userDataMap)
                    .await()
            } else {
                userDocRef
                    .document(userData.id)
                    .set(userDataMap)
                    .await()
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

    override suspend fun updateNotificationToken(userId: String, newToken: String): Result<Unit> {
        return try {
            val usersCollection = firestore.collection(USERS_COLLECTION)
            val userDocRef = usersCollection.document(userId)
            val snapshot = userDocRef.get().await()
            if (snapshot.exists()) {
                userDocRef
                    .update(NOTIFICATIONS_TOKEN_FIELD, newToken)
                    .await()
                Result.success(Unit)
            } else {
                Result.failure(UserNotFoundException())
            }
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }
}
