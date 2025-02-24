package com.section11.expenselens.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Pending
import com.section11.expenselens.domain.models.UserHousehold
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


class FirestoreHouseholdInvitationRepositoryTest {

    private lateinit var repository: FirestoreHouseholdInvitationRepository

    private val mockFirestore: FirebaseFirestore = mock()
    private val usersCollectionMock: CollectionReference = mock()

    @Before
    fun setUp() {
        whenever(mockFirestore.collection(USERS_COLLECTION)).thenReturn(usersCollectionMock)
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
        val householdId = "householdId"
        val householdName = "Test Household"
        val inviterId = "inviterId"
        val status = Pending
        val timestamp = Timestamp(seconds=1740070644, nanoseconds=583000000)
        val invitationMap = mapOf(
            "householdId" to householdId,
            "householdName" to householdName,
            "inviterId" to inviterId,
            "status" to status.name,
            "timestamp" to timestamp
        )
        val listOfInvitations = listOf(invitationMap)
        whenever(docSnapShotMock.get(anyString())).thenReturn(listOfInvitations)

        val result = repository.getPendingInvitations(userId)

        assertThat(result.isSuccess).isTrue()
        val resultList = result.getOrNull()
        assert(resultList is List<HouseholdInvite>)
        val invite = resultList?.first()
        assertThat(invite).isNotNull()
        assertThat(invite?.householdId).isEqualTo(householdId)
        assertThat(invite?.householdName).isEqualTo(householdName)
        assertThat(invite?.inviterId).isEqualTo(inviterId)
        assertThat(invite?.status).isEqualTo(status)
        assertThat(invite?.timestamp).isEqualTo(timestamp)
    }
}
