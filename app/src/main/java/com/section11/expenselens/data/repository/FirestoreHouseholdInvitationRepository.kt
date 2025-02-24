package com.section11.expenselens.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.EMAIL_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.HOUSEHOLD_ID_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.HOUSEHOLD_NAME_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.INVITATIONS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.INVITER_ID_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.INVITE_STATUS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.INVITE_TIMESTAMP_FIELD
import com.section11.expenselens.domain.exceptions.NullFieldOnFirebaseException
import com.section11.expenselens.domain.exceptions.UserNotFoundException
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Pending
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdInvitationRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val USER_NOT_FOUND_MESSAGE = "User not found"

class FirestoreHouseholdInvitationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : HouseholdInvitationRepository {

    private val nullException = NullFieldOnFirebaseException()

    override suspend fun postInvitationsToUser(
        inviterId: String,
        inviteeEmail: String,
        household: UserHousehold
    ): Result<Unit> {
        return try {
            val usersCollection = firestore.collection(USERS_COLLECTION)
            val querySnapshot = usersCollection
                .whereEqualTo(EMAIL_FIELD, inviteeEmail)
                .get()
                .await()

            if (querySnapshot.isEmpty) return Result.failure(getUserNotFoundException())

            val inviteeId = querySnapshot.documents.first().id

            val invitation = mapOf(
                HOUSEHOLD_ID_FIELD to household.id,
                HOUSEHOLD_NAME_FIELD to household.name,
                INVITER_ID_FIELD to inviterId,
                INVITE_STATUS_FIELD to Pending,
                INVITE_TIMESTAMP_FIELD to Timestamp.now()
            )

            firestore.collection(USERS_COLLECTION)
                .document(inviteeId)
                .update(INVITATIONS_FIELD, FieldValue.arrayUnion(invitation))
                .await()

            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    private fun getUserNotFoundException(): UserNotFoundException {
        return UserNotFoundException(USER_NOT_FOUND_MESSAGE)
    }

    override suspend fun getPendingInvitations(userId: String): Result<List<HouseholdInvite>> {
        return try {
            val userDoc = firestore.collection(USERS_COLLECTION).document(userId).get().await()
            val queriedInvitationList = userDoc.get(INVITATIONS_FIELD) as? List<Map<String, Any>>



            val inviteList = queriedInvitationList?.map { invitationMap ->
                val householdId = invitationMap.getOrThrow<String>(HOUSEHOLD_ID_FIELD)
                val householdName = invitationMap.getOrThrow<String>(HOUSEHOLD_NAME_FIELD)
                val inviterId = invitationMap.getOrThrow<String>(INVITER_ID_FIELD)
                val status = HouseholdInviteStatus.valueOf(invitationMap.getOrThrow(INVITE_STATUS_FIELD))
                val timestamp = invitationMap.getOrThrow<Timestamp>(INVITE_TIMESTAMP_FIELD)

                HouseholdInvite(
                    householdId = householdId,
                    householdName = householdName,
                    inviterId = inviterId,
                    status = status,
                    timestamp = timestamp
                )
            }

            Result.success(inviteList ?: emptyList())
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        } catch (exception: NullFieldOnFirebaseException) {
            Result.failure(exception)
        }
    }

    @Throws(NullFieldOnFirebaseException::class)
    private fun <T> Map<String, Any>.getOrThrow(key: String): T {
        return this[key] as? T ?: throw nullException
    }
}
