package com.section11.expenselens.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Fields.HOUSEHOLDS_FIELD
import com.section11.expenselens.domain.models.UserHousehold
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class FirestoreUsersHouseholdsRepositoryTest {

    private lateinit var repository: FirestoreUsersHouseholdsRepository

    private val mockFirestore: FirebaseFirestore = mock()
    private val mockDocumentReference: DocumentReference = mock()
    private val mockDocumentSnapshot: DocumentSnapshot = mock()
    private val mockTransaction: Transaction = mock()

    private val testUserId = "testUserId"
    private val testHousehold = UserHousehold(id = "household1", name = "Test Household")

    @Before
    fun setUp() {
        repository = FirestoreUsersHouseholdsRepository(mockFirestore)

        whenever(mockFirestore.collection(USERS_COLLECTION)).thenReturn(mock())
        whenever(mockFirestore.collection(USERS_COLLECTION).document(testUserId))
            .thenReturn(mockDocumentReference)
    }

    @Test
    fun `getUserHouseholds returns empty list when document does not exist`() = runTest {
        whenever(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
        whenever(mockDocumentSnapshot.exists()).thenReturn(false)

        val result = repository.getUserHouseholds(testUserId)

        assertThat(result).isEmpty()
    }

    @Test
    fun `getUserHouseholds returns list of households when data is available`() = runTest {
        val fakeHouseholds = listOf(
            mapOf("id" to "household1", "name" to "Test Household 1"),
            mapOf("id" to "household2", "name" to "Test Household 2")
        )

        whenever(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.get(HOUSEHOLDS_FIELD)).thenReturn(fakeHouseholds)

        val result = repository.getUserHouseholds(testUserId)

        assertThat(result).containsExactly(
            UserHousehold("household1", "Test Household 1"),
            UserHousehold("household2", "Test Household 2")
        )
    }

    @Test
    fun `addHouseholdToUser creates new document if user does not exist`() = runTest {
        doAnswer { invocation ->
            val transactionFunction = invocation.arguments[0] as Transaction.Function<Unit>
            transactionFunction.apply(mockTransaction) // Call it with the mock transaction
            Tasks.forResult(Unit) // Return a successful Task
        }.whenever(mockFirestore).runTransaction(any<Transaction.Function<Unit>>())

        whenever(mockTransaction.get(mockDocumentReference)).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(false)

        val result = repository.addHouseholdToUser(testUserId, testHousehold)

        verify(mockTransaction).set(mockDocumentReference, mapOf(HOUSEHOLDS_FIELD to listOf(testHousehold)))
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `addHouseholdToUser updates document when household doesn't exist`() = runTest {
        val existingHousehold = UserHousehold("someHouseholdId", "Test Household 1")
        val existingHouseholds = listOf(
            mapOf("id" to existingHousehold.id, "name" to existingHousehold.name)
        )

        doAnswer { invocation ->
            val transactionFunction = invocation.arguments[0] as Transaction.Function<Unit>
            transactionFunction.apply(mockTransaction) // Call it with the mock transaction
            Tasks.forResult(Unit) // Return a successful Task
        }.whenever(mockFirestore).runTransaction(any<Transaction.Function<Unit>>())


        whenever(mockTransaction.get(mockDocumentReference)).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.get(HOUSEHOLDS_FIELD)).thenReturn(existingHouseholds)

        val result = repository.addHouseholdToUser(testUserId, testHousehold)

        verify(mockTransaction).update(
            mockDocumentReference,
            HOUSEHOLDS_FIELD,
            listOf(
                existingHousehold,
                testHousehold
            )
        )
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `addHouseholdToUser does not add duplicate household`() = runTest {
        val existingHouseholds = listOf(
            mapOf("id" to "household1", "name" to "Test Household 1")
        )

        doAnswer { invocation ->
            val transactionFunction = invocation.arguments[0] as Transaction.Function<Unit>
            transactionFunction.apply(mockTransaction) // Call it with the mock transaction
            Tasks.forResult(Unit) // Return a successful Task
        }.whenever(mockFirestore).runTransaction(any<Transaction.Function<Unit>>())
        whenever(mockTransaction.get(mockDocumentReference)).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.get(HOUSEHOLDS_FIELD)).thenReturn(existingHouseholds)

        val result = repository.addHouseholdToUser(testUserId, UserHousehold("household1", "Test Household 1"))

        verify(mockTransaction, never()).update(any<DocumentReference>(), any<String>(), any())
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `addHouseholdToUser returns failure when transaction fails`() = runTest {
        val mockException:  FirebaseFirestoreException = mock()
        doAnswer { invocation ->
            val transactionFunction = invocation.arguments[0] as Transaction.Function<Unit>
            transactionFunction.apply(mockTransaction) // Call it with the mock transaction
            Tasks.forResult(Unit) // Return a successful Task
        }.whenever(mockFirestore).runTransaction(any<Transaction.Function<Unit>>())
        whenever(mockTransaction.get(mockDocumentReference)).thenThrow(mockException)

        val result = repository.addHouseholdToUser(testUserId, testHousehold)

        assertThat(result.isFailure).isTrue()
    }
}
