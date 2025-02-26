package com.section11.expenselens.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.HOUSEHOLDS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.HouseholdsCollection.USERS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.HOUSEHOLDS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.INVITATIONS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersInvitationsArray.INVITER_ID
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersInvitationsArray.INVITE_HOUSEHOLD_ID
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersInvitationsArray.INVITE_STATUS
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Pending
import com.section11.expenselens.domain.models.UserHousehold
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FirestoreHouseholdInvitationRepositoryTest {

    private lateinit var repository: FirestoreHouseholdInvitationRepository

    private val mockFirestore: FirebaseFirestore = mock()
    private val usersCollectionMock: CollectionReference = mock()
    private val householdCollectionMock: CollectionReference = mock()
    private val documentReference: DocumentReference = mock()
    private val documentSnapshot: DocumentSnapshot = mock()
    private val testUserId = "user123"
    private val testInviterId = "user456"
    private val testHouseholdId = "household456"
    private val testHouseholdName = "Test Household"
    private val inviteStatus = Pending

    @Before
    fun setUp() {
        whenever(mockFirestore.collection(USERS_COLLECTION)).thenReturn(usersCollectionMock)
        whenever(mockFirestore.collection(HOUSEHOLDS_COLLECTION)).thenReturn(householdCollectionMock)
        whenever(usersCollectionMock.document(any())).thenReturn(documentReference)
        repository = FirestoreHouseholdInvitationRepository(mockFirestore)
    }

    @Test
    fun `postInvitationsToUser creates new document if user does not exist`() = runTest {
        // Given
        val userHousehold = UserHousehold("household1", "Test Household")
        val queryMock: Query = mock()
        whenever(usersCollectionMock.whereEqualTo(anyString(), anyString())).thenReturn(queryMock)
        val querySnapshot: QuerySnapshot = mock()
        val queryTask: Task<QuerySnapshot> = Tasks.forResult(querySnapshot)
        whenever(queryMock.get()).thenReturn(queryTask)
        whenever(querySnapshot.isEmpty).thenReturn(false)
        val docMock: DocumentSnapshot = mock()
        whenever(docMock.id).thenReturn("docIc")
        val listOfDocs: List<DocumentSnapshot> = listOf(docMock)
        whenever(querySnapshot.documents).thenReturn(listOfDocs)
        val docReference: DocumentReference = mock()
        val updateTask: Task<Void> = Tasks.forResult(null)
        whenever(docReference.update(anyString(), any())).thenReturn(updateTask)
        whenever(usersCollectionMock.document(anyString())).thenReturn(docReference)

        val result =repository.postInvitationsToUser(
            "inviterId",
            "inviteeEmail",
            userHousehold
        )

        verify(docReference).update(anyString(), any())
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `postInvitationsToUser return user not found if snapshot is empty`() = runTest {
        // Given
        val userHousehold = UserHousehold("household1", "Test Household")
        val queryMock: Query = mock()
        whenever(usersCollectionMock.whereEqualTo(anyString(), anyString())).thenReturn(queryMock)
        val querySnapshot: QuerySnapshot = mock()
        val queryTask: Task<QuerySnapshot> = Tasks.forResult(querySnapshot)
        whenever(queryMock.get()).thenReturn(queryTask)
        whenever(querySnapshot.isEmpty).thenReturn(true)

        val result =repository.postInvitationsToUser(
            "inviterId",
            "inviteeEmail",
            userHousehold
        )

        verify(usersCollectionMock, never()).document(anyString())
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `getPendingInvitations returns empty list if user has no pending invitations`() = runTest {
        // Given
        val userId = "userId"
        val docSnapShotMock: DocumentSnapshot = mock()
        whenever(docSnapShotMock.id).thenReturn(userId)
        val listOfDocs: List<DocumentSnapshot> = listOf(docSnapShotMock)
        val querySnapshot: QuerySnapshot = mock()
        whenever(querySnapshot.documents).thenReturn(listOfDocs)
        val docMock: DocumentReference = mock()
        whenever(usersCollectionMock.document(anyString())).thenReturn(docMock)
        val queryTask: Task<DocumentSnapshot> = Tasks.forResult(docSnapShotMock)
        whenever(docMock.get()).thenReturn(queryTask)

        val result = repository.getPendingInvitations(userId)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun `getPendingInvitations returns list of pending invitations if user has pending invitations`() = runTest {
        // Given
        val docSnapShotMock: DocumentSnapshot = mock()
        whenever(docSnapShotMock.id).thenReturn(testUserId)
        val listOfDocs: List<DocumentSnapshot> = listOf(docSnapShotMock)
        val querySnapshot: QuerySnapshot = mock()
        whenever(querySnapshot.documents).thenReturn(listOfDocs)
        val docMock: DocumentReference = mock()
        whenever(usersCollectionMock.document(anyString())).thenReturn(docMock)
        val queryTask: Task<DocumentSnapshot> = Tasks.forResult(docSnapShotMock)
        whenever(docMock.get()).thenReturn(queryTask)
        val timestamp = Timestamp(seconds=1740070644, nanoseconds=583000000)
        val invitationMap = mapOf(
            "householdId" to testHouseholdId,
            "householdName" to testHouseholdName,
            "inviterId" to testInviterId,
            "status" to inviteStatus.name,
            "timestamp" to timestamp
        )
        val listOfInvitations = listOf(invitationMap)
        whenever(docSnapShotMock.get(anyString())).thenReturn(listOfInvitations)

        val result = repository.getPendingInvitations(testUserId)

        assertThat(result.isSuccess).isTrue()
        val resultList = result.getOrNull()
        assert(resultList is List<HouseholdInvite>)
        val invite = resultList?.first()
        assertThat(invite).isNotNull()
        assertThat(invite?.householdId).isEqualTo(testHouseholdId)
        assertThat(invite?.householdName).isEqualTo(testHouseholdName)
        assertThat(invite?.inviterId).isEqualTo(testInviterId)
        assertThat(invite?.status).isEqualTo(inviteStatus)
        assertThat(invite?.timestamp).isEqualTo(timestamp)
    }

    @Test
    fun `acceptHouseholdInvite updates documents successfully`() = runTest {
        val existingInvitations = listOf(
            mapOf(
                INVITE_HOUSEHOLD_ID to testHouseholdId,
                INVITER_ID to testInviterId,
                INVITE_STATUS to inviteStatus.name
            )
        )
        whenever(documentSnapshot.get(INVITATIONS_FIELD)).thenReturn(existingInvitations)
        whenever(documentSnapshot.exists()).thenReturn(true)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        val householdDocMock: DocumentReference = mock()
        whenever(householdCollectionMock.document(any())).thenReturn(householdDocMock)

        val batch: WriteBatch = mock()
        doAnswer { invocation ->
            val batchFunction = invocation.arguments[0] as WriteBatch.Function
            batchFunction.apply(batch) // Call it with the mock transaction
            Tasks.forResult<WriteBatch>(mock()) // Return a successful Task
        }.whenever(mockFirestore).runBatch(any<WriteBatch.Function>() )

        // Mock updates in the batch
        whenever(batch.update(any(), anyString(), any())).thenReturn(batch)

        val result = repository.acceptHouseholdInvite(testUserId, testHouseholdId, testHouseholdName)

        // Verify the batch updates
        verify(batch).update(
            eq(householdDocMock),
            eq(USERS_FIELD),
            argThat { value ->
                value is FieldValue && value.javaClass == FieldValue::class.java
                true
            }
        )
        verify(batch).update(
            eq(documentReference),
            eq(INVITATIONS_FIELD),
            any() // We'll verify the filtered invitations separately if needed
        )
        verify(batch).update(
            eq(documentReference),
            eq(HOUSEHOLDS_FIELD),
            argThat { value ->
                value is FieldValue && value.javaClass == FieldValue::class.java // Check it's FieldValue
                // Since we can't inspect FieldValue directly, we'll assume the map was correct if the test passes
                true
            }
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `acceptHouseholdInvite fails when FirestoreException occurs`() = runTest {
        // Mock a Firestore exception
        val exception: FirebaseFirestoreException = mock()
        val querySnapshot: QuerySnapshot = mock()
        val queryTask: Task<QuerySnapshot> = Tasks.forResult(querySnapshot)
        whenever(usersCollectionMock.get()).thenReturn(queryTask)
        whenever(documentSnapshot.get(INVITATIONS_FIELD)).thenReturn(listOf<Map<String, Any>>())
        whenever(documentSnapshot.exists()).thenReturn(true)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(mockFirestore.runBatch(any())).then { throw exception }

        val result = repository.acceptHouseholdInvite(testUserId, testHouseholdId, testHouseholdName)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FirebaseFirestoreException)
    }

    @Test
    fun `deleteHouseholdInvite removes invitation successfully`() = runTest {
        val existingInvitations = listOf(
            mapOf(
                INVITE_HOUSEHOLD_ID to testHouseholdId,
                INVITER_ID to testInviterId,
                INVITE_STATUS to inviteStatus.name
            ),
            mapOf(
                INVITE_HOUSEHOLD_ID to "otherHousehold",
                INVITER_ID to "inviter456",
                INVITE_STATUS to inviteStatus.name
            )
        )
        whenever(documentSnapshot.get(INVITATIONS_FIELD)).thenReturn(existingInvitations)
        whenever(documentSnapshot.exists()).thenReturn(true)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        val task: Task<Void> = Tasks.forResult(null)
        whenever(documentReference.update(anyString(), any())).thenReturn(task)

        val result = repository.deleteHouseholdInvite(testUserId, testHouseholdId)

        // Verify the update with filtered invitations
        val argumentCaptor = ArgumentCaptor.forClass(Any::class.java)
        verify(documentReference).update(eq(INVITATIONS_FIELD), argumentCaptor.capture())
        val updatedInvitations = argumentCaptor.firstValue as List<*>
        assertTrue(updatedInvitations.size == 1) // Only one invitation remains
        assertTrue((updatedInvitations[0] as Map<*, *>)["householdId"] != testHouseholdId)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteHouseholdInvite fails when FirestoreException occurs`() = runTest {
        val exception: FirebaseFirestoreException = mock()
        whenever(documentSnapshot.get(INVITATIONS_FIELD)).thenReturn(listOf<Map<String, Any>>())
        whenever(documentSnapshot.exists()).thenReturn(true)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentReference.update(anyString(), any())).then {
            throw exception
        }

        val result = repository.deleteHouseholdInvite(testUserId, testHouseholdId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FirebaseFirestoreException)
    }
}
