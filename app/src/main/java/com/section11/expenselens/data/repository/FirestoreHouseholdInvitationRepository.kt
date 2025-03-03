package com.section11.expenselens.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.HOUSEHOLDS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.HouseholdsCollection.USERS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.EMAIL_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.HOUSEHOLDS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.INVITATIONS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_ID_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_NAME_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersInvitationsArray.INVITER_ID
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersInvitationsArray.INVITE_HOUSEHOLD_ID
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersInvitationsArray.INVITE_HOUSEHOLD_NAME
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersInvitationsArray.INVITE_STATUS
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersInvitationsArray.INVITE_TIMESTAMP
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

            // Generate a new invite document ID
            val inviteId = firestore.collection(USERS_COLLECTION)
                .document(inviteeId)
                .collection(INVITATIONS_FIELD)
                .document().id // Generate a unique Firestore document ID

            // Create the invitation document
            val invitation = mapOf(
                INVITE_HOUSEHOLD_ID to household.id,
                INVITE_HOUSEHOLD_NAME to household.name,
                INVITER_ID to inviterId,
                INVITE_STATUS to Pending,
                INVITE_TIMESTAMP to Timestamp.now()
            )

            firestore.collection(USERS_COLLECTION)
                .document(inviteeId)
                .collection(INVITATIONS_FIELD)
                .document(inviteId)
                .set(invitation)
                .await()

            Result.success(Unit)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }

    private fun getUserNotFoundException(): UserNotFoundException {
        return UserNotFoundException(USER_NOT_FOUND_MESSAGE)
    }

    override suspend fun getPendingInvitations(userId: String): Result<List<HouseholdInvite>> {
        return try {
            val invitationsCollection = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(INVITATIONS_FIELD)

            val querySnapshot = invitationsCollection
                .whereEqualTo(INVITE_STATUS, Pending)
                .get()
                .await()

            val inviteList = querySnapshot.documents.map { doc ->
                val householdId = doc.getOrThrow<String>(INVITE_HOUSEHOLD_ID)
                val householdName = doc.getOrThrow<String>(INVITE_HOUSEHOLD_NAME)
                val inviterId = doc.getOrThrow<String>(INVITER_ID)
                val status = HouseholdInviteStatus.valueOf(doc.getOrThrow(INVITE_STATUS))
                val timestamp = doc.getOrThrow<Timestamp>(INVITE_TIMESTAMP)

                HouseholdInvite(
                    inviteId = doc.id,
                    householdId = householdId,
                    householdName = householdName,
                    inviterId = inviterId,
                    status = status,
                    timestamp = timestamp
                )
            }

            Result.success(inviteList)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        } catch (exception: NullFieldOnFirebaseException) {
            Result.failure(exception)
        }
    }

    @Throws(NullFieldOnFirebaseException::class)
    private fun <T> DocumentSnapshot.getOrThrow(key: String): T {
        return this[key] as? T ?: throw nullException
    }

    /**
     * This method runs a batch of updates on firestore.
     *
     * Basically when we accept an Invitation to a household 3 things need to happen:
     * - delete invitation
     * - Add user to household/{householdId}/users array.
     * - Add household to users/{userId}/households
     *
     * NOTE: this might seem redundant. But since the app is very READ heavy is better to have
     * this trade off, since it favor reading values. If you need to know the household of a specific
     * user you don't need to go through EACH household. And if you need to show the users of a
     * household you don't need to go through EACH user.
     * Basically is expected to get that information more often than having invites. So that's the
     * trade off
     *
     * What this method does is run a batch of updates, so in case 1 fails all fail, and not risk
     * having 1 of 3 updates fail and end up with inconsistent data.
     */
    override suspend fun acceptHouseholdInvite(
        inviteId: String,
        userId: String,
        householdId: String,
        householdName: String
    ): Result<Unit> {
        return try {
            val userCollection = firestore.collection(USERS_COLLECTION).document(userId)
            val inviteDocRef = userCollection.collection(INVITATIONS_FIELD).document(inviteId)

            firestore.runBatch { batch ->
                // Delete the invitation document
                batch.delete(inviteDocRef)

                // Add household to user
                val householdMap = mapOf(
                    HOUSEHOLD_ID_FIELD to householdId,
                    HOUSEHOLD_NAME_FIELD to householdName
                )
                batch.update(userCollection, HOUSEHOLDS_FIELD, FieldValue.arrayUnion(householdMap))

                // Add user to household
                val householdRef = firestore.collection(HOUSEHOLDS_COLLECTION).document(householdId)
                batch.update(householdRef, USERS_FIELD, FieldValue.arrayUnion(userId))
            }.await()

            Result.success(Unit)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }

    override suspend fun deleteHouseholdInvite(
        inviteId: String,
        userId: String,
        householdId: String
    ): Result<Unit> {
        return try {
            val inviteDocRef = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(INVITATIONS_FIELD)
                .document(inviteId)

            inviteDocRef.delete().await()

            Result.success(Unit)
        } catch (exception: FirebaseFirestoreException) {
            Result.failure(exception)
        }
    }
}
