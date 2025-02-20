package com.section11.expenselens.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
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
//        val docMock: DocumentSnapshot = mock()
//        whenever(docMock.id).thenReturn("docIc")
//        val listOfDocs: List<DocumentSnapshot> = listOf(docMock)
//        whenever(querySnapshot.documents).thenReturn(listOfDocs)
//        val docReference: DocumentReference = mock()
//        val updateTask: Task<Void> = Tasks.forResult(null)
//        whenever(docReference.update(anyString(), any())).thenReturn(updateTask)
//        whenever(usersCollectionMock.document(anyString())).thenReturn(docReference)

        val result =repository.postInvitationsToUser(
            "inviterId",
            "inviteeEmail",
            userHousehold
        )

        verify(usersCollectionMock, never()).document(anyString())
        assertThat(result.isFailure).isTrue()
    }
}
