package com.section11.expenselens.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.HOUSEHOLDS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.ID_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.NAME_FIELD
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.UserHouseholdsRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUsersHouseholdsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserHouseholdsRepository {

    override suspend fun getUserHouseholds(userId: String): List<UserHousehold> {
        val userDoc = firestore.collection(USERS_COLLECTION).document(userId).get().await()
        val households = (userDoc.get(HOUSEHOLDS_FIELD) as? List<*>)?.mapToHouseholdsList()

        return households ?: emptyList()
    }

    private fun List<*>.mapToHouseholdsList(): List<UserHousehold> {
        return mapNotNull {
            val map = it as? Map<*, *>
            val id = map?.get(ID_FIELD) as? String
            val name = map?.get(NAME_FIELD) as? String
            if (id != null && name != null) UserHousehold(id, name) else null
        }
    }

    override suspend fun addHouseholdToUser(
        userId: String,
        household: UserHousehold
    ): Result<Unit> {
        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                val userHouseholds = if (snapshot.exists()) {
                    (snapshot.get(HOUSEHOLDS_FIELD) as? List<*>)?.mapToHouseholdsList() ?: emptyList()
                } else {
                    emptyList()
                }

                // Avoid duplicate households
                if (userHouseholds.any { it.id == household.id }) {
                    return@runTransaction
                }

                val updatedHouseholds = userHouseholds.toMutableList().apply { add(household) }

                if (snapshot.exists()) {
                    transaction.update(userDocRef, HOUSEHOLDS_FIELD, updatedHouseholds)
                } else {
                    transaction.set(userDocRef, mapOf(HOUSEHOLDS_FIELD to updatedHouseholds))
                }
            }.await()

            Result.success(Unit)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }
}
