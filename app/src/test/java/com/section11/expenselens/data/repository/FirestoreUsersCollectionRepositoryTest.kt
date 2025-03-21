package com.section11.expenselens.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.USERS_COLLECTION
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.EMAIL_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.HOUSEHOLDS_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.NOTIFICATIONS_TOKEN_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_ID_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_NAME_FIELD
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.ui.utils.getUserData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class FirestoreUsersCollectionRepositoryTest {

    private lateinit var repository: FirestoreUsersCollectionRepository

    private val mockFirestore: FirebaseFirestore = mock()
    private val mockDocumentReference: DocumentReference = mock()
    private val mockDocumentSnapshot: DocumentSnapshot = mock()
    private val mockTransaction: Transaction = mock()
    private val mockUserCollection: CollectionReference = mock()

    private val testUserId = "testUserId"
    private val testHousehold = UserHousehold(id = "household1", name = "Test Household")

    @Before
    fun setUp() {
        repository = FirestoreUsersCollectionRepository(mockFirestore)

        whenever(mockFirestore.collection(USERS_COLLECTION)).thenReturn(mockUserCollection)
        whenever(mockFirestore.collection(USERS_COLLECTION).document(testUserId))
            .thenReturn(mockDocumentReference)
    }

    @Test
    fun `createOrUpdateUser calls set when snapshot doesn't exist`() = runTest {
        val userData = getUserData()
        whenever(mockUserCollection.document(any())).thenReturn(mockDocumentReference)
        val task: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
        whenever(mockDocumentReference.get()).thenReturn(task)
        whenever(mockDocumentSnapshot.exists()).thenReturn(false)
        val setTask: Task<Void> = Tasks.forResult(null)
        whenever(mockDocumentReference.set(any())).thenReturn(setTask)

        val result = repository.createOrUpdateUser(userData)

        assertThat(result.isSuccess).isTrue()
        val captor = argumentCaptor<Map<String, Any>>()
        verify(mockDocumentReference).set(captor.capture())
        assertThat(captor.firstValue[HOUSEHOLDS_FIELD]).isEqualTo(emptyList<Map<String, Any>>())
        assertThat(captor.firstValue[EMAIL_FIELD]).isEqualTo(userData.email)
        assertThat(captor.firstValue[NOTIFICATIONS_TOKEN_FIELD]).isEqualTo(userData.notificationToken)
        assertThat(captor.firstValue[HOUSEHOLDS_FIELD]).isEqualTo(emptyList<Map<String, Any>>())
    }

    @Test
    fun `createOrUpdateUser calls update when snapshot exists`() = runTest {
        val userData = getUserData()
        whenever(mockUserCollection.document(any())).thenReturn(mockDocumentReference)
        val task: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
        whenever(mockDocumentReference.get()).thenReturn(task)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        val setTask: Task<Void> = Tasks.forResult(null)
        whenever(mockDocumentReference.update(any())).thenReturn(setTask)

        val result = repository.createOrUpdateUser(userData)

        assertThat(result.isSuccess).isTrue()
        val captor = argumentCaptor<Map<String, Any>>()
        verify(mockDocumentReference).update(captor.capture())
        assertThat(captor.firstValue[HOUSEHOLDS_FIELD]).isEqualTo(emptyList<Map<String, Any>>())
        assertThat(captor.firstValue[EMAIL_FIELD]).isEqualTo(userData.email)
        assertThat(captor.firstValue[NOTIFICATIONS_TOKEN_FIELD]).isEqualTo(userData.notificationToken)
        assertThat(captor.firstValue[HOUSEHOLDS_FIELD]).isEqualTo(emptyList<Map<String, Any>>())
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
            eq(mockDocumentReference),
            eq(HOUSEHOLDS_FIELD),
            any<FieldValue>()
        )
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `addHouseholdToUser returns failure when transaction fails`() = runTest {
        val mockException: FirebaseFirestoreException = mock()
        doAnswer {
            throw mockException
        }.whenever(mockFirestore).runTransaction(any<Transaction.Function<Unit>>())

        val result = repository.addHouseholdToUser(testUserId, testHousehold)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `updateNotificationToken updates document with new token`() = runTest {
        val newToken = "newToken"
        val userDocRef: DocumentReference = mock()
        whenever(mockUserCollection.document(anyString())).thenReturn(userDocRef)
        whenever(userDocRef.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(userDocRef.update(anyString(), any())).thenReturn(Tasks.forResult(null))

        val result = repository.updateNotificationToken(testUserId, newToken)

        assertThat(result.isSuccess).isTrue()
        verify(userDocRef).update(NOTIFICATIONS_TOKEN_FIELD, newToken)
    }

    @Test
    fun `updateNotificationToken if user doesn't exist returns failure`() = runTest {
        val newToken = "newToken"
        val userDocRef: DocumentReference = mock()
        whenever(mockUserCollection.document(anyString())).thenReturn(userDocRef)
        whenever(userDocRef.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
        whenever(mockDocumentSnapshot.exists()).thenReturn(false)

        val result = repository.updateNotificationToken(testUserId, newToken)

        assertThat(result.isFailure).isTrue()
        verify(userDocRef, never()).update(NOTIFICATIONS_TOKEN_FIELD, newToken)
    }

    @Test
    fun `updateNotificationToken returns failure when firebase exception`() = runTest {
        val newToken = "new Token"
        val mockFirebaseException: FirebaseFirestoreException = mock()
        whenever(mockUserCollection.document(anyString())).then {
            throw mockFirebaseException
        }

        val result = repository.updateNotificationToken(testUserId, newToken)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `getListOfUserEmails returns list of emails`() = runTest {
        val userIds = listOf("id", "id2")
        val fakeEmails = listOf("email1", "email2")
        userIds.forEachIndexed { index, idString ->
            val docRef: DocumentReference = mock()
            val docSnapshot: DocumentSnapshot = mock()
            whenever(docSnapshot.getString(EMAIL_FIELD)).thenReturn(fakeEmails[index])
            val task = Tasks.forResult(docSnapshot)
            whenever(docRef.get()).thenReturn(task)
            whenever(mockUserCollection.document(idString)).thenReturn(docRef)
        }

        val result = repository.getListOfUserEmails(userIds)

        assert(result.isEmpty().not())
        assert(result.size == fakeEmails.size)
        result.forEachIndexed { index, email ->
            assertThat(email).isEqualTo(fakeEmails[index])
        }
    }

    @Test
    fun `getListOfUserEmails returns empty list when no emails found`() = runTest {
        val userIds = listOf("id", "id2")
        userIds.forEach { idString ->
            val docRef: DocumentReference = mock()
            val docSnapshot: DocumentSnapshot = mock()
            whenever(docSnapshot.getString(EMAIL_FIELD)).thenReturn(null)
            val task = Tasks.forResult(docSnapshot)
            whenever(docRef.get()).thenReturn(task)
            whenever(mockUserCollection.document(idString)).thenReturn(docRef)
        }

        val result = repository.getListOfUserEmails(userIds)

        assert(result.isEmpty())
    }

    @Test
    fun `removeHouseholdFromUser removes household from user document`() = runTest {
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
        whenever(mockDocumentSnapshot.get(HOUSEHOLDS_FIELD)).thenReturn(existingHouseholds)

        val result = repository.removeHouseholdFromUser(testUserId, existingHousehold.id)

        verify(mockTransaction).update(
            mockDocumentReference,
            HOUSEHOLDS_FIELD,
            emptyList<String>()
        )
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `removeHouseholdFromUser removes household from user document with more than 1 household`() = runTest {
        val existingHousehold = UserHousehold("someHouseholdId", "Test Household 1")
        val someOtherHousehold = UserHousehold("someOtherHouseholdId", "Test Household 2")
        val existingHouseholds = listOf(
            mapOf(
                HOUSEHOLD_ID_FIELD to existingHousehold.id,
                HOUSEHOLD_NAME_FIELD to existingHousehold.name
            ),
            mapOf(
                HOUSEHOLD_ID_FIELD to someOtherHousehold.id,
                HOUSEHOLD_NAME_FIELD to someOtherHousehold.name
            )
        )

        doAnswer { invocation ->
            val transactionFunction = invocation.arguments[0] as Transaction.Function<Unit>
            transactionFunction.apply(mockTransaction) // Call it with the mock transaction
            Tasks.forResult(Unit) // Return a successful Task
        }.whenever(mockFirestore).runTransaction(any<Transaction.Function<Unit>>())

        whenever(mockTransaction.get(mockDocumentReference)).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.get(HOUSEHOLDS_FIELD)).thenReturn(existingHouseholds)

        val result = repository.removeHouseholdFromUser(testUserId, existingHousehold.id)

        verify(mockTransaction).update(
            mockDocumentReference,
            HOUSEHOLDS_FIELD,
            listOf(someOtherHousehold)
        )
        assertThat(result.isSuccess).isTrue()
    }
}
