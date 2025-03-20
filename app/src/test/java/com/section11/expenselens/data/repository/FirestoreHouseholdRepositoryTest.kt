package com.section11.expenselens.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.HouseholdsCollection.USERS_FIELD
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.data.dto.FirestoreHousehold
import com.section11.expenselens.data.mapper.toDomainExpense
import com.section11.expenselens.domain.models.Category.HOME
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.models.UserData
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

private const val HOUSEHOLD_COLLECTION = "households"
private const val EXPENSES_COLLECTION = "expenses"

@ExperimentalCoroutinesApi
class FirestoreHouseholdRepositoryTest {

    private lateinit var repository: FirestoreHouseholdRepository
    private val mockFirestore: FirebaseFirestore = mock()
    private val mockCollection: CollectionReference = mock()
    private val mockDocument: DocumentReference = mock()

    @Before
    fun setUp() {
        repository = FirestoreHouseholdRepository(mockFirestore)
    }

    @Test
    fun `addExpenseToHousehold returns success when added successfully`() = runTest {
        // Given
        val userData: UserData = mock()
        val userId = "user123"
        val userName = "username"
        whenever(userData.id).thenReturn(userId)
        whenever(userData.displayName).thenReturn(userName)
        val householdId = "household456"
        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )

        // Mock Firestore collection
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.collection(EXPENSES_COLLECTION)).thenReturn(mockCollection)

        // Mock Firestore add() operation
        val mockTask: Task<DocumentReference> = Tasks.forResult(mockDocument)
        whenever(mockCollection.add(any())).thenReturn(mockTask)

        // When
        val result = repository.addExpenseToHousehold(userData, householdId, expense)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `createHousehold returns success with ID and name`() = runTest {
        // Given
        val householdName = "New Household"
        val userId = "user123"
        val householdId = "household456"

        val mockDocument: DocumentReference = mock()
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document()).thenReturn(mockDocument)
        whenever(mockCollection.document(any())).thenReturn(mockDocument)
        whenever(mockDocument.id).thenReturn(householdId)
        whenever(mockDocument.set(any())).thenReturn(Tasks.forResult(null))

        // When
        val result = repository.createHousehold(userId, householdName)

        // Then
        assertTrue(result.isSuccess)
        val household = result.getOrNull()
        assertEquals(householdId, household?.id)
        assertEquals(householdName, household?.name)
    }

    @Test
    fun `createHousehold returns failure on exception`() = runTest {
        // Given
        val householdName = "New Household"
        val userId = "user123"
        val mockDocument: DocumentReference = mock()
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document()).thenReturn(mockDocument)
        whenever(mockDocument.id).thenReturn("household456")
        val mockTask: Task<DocumentReference> = Tasks.forResult(mockDocument)
        whenever(mockCollection.add(any())).thenReturn(mockTask)
        whenever(mockCollection.document(anyString())).thenReturn(mockDocument)
        val firebaseException: FirebaseFirestoreException = mock()
        whenever(mockDocument.set(any())).thenReturn(Tasks.forException(firebaseException))

        // When
        val result = repository.createHousehold(userId, householdName)
        advanceUntilIdle()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `getAllExpensesFromHousehold returns list of expenses`() = runTest {
        // Given
        val householdId = "household456"
        val expenseList = listOf(
            FirestoreExpense(
                category = HOME.displayName,
                total = 100.0,
                timestamp = Timestamp(Date()),
                userId = "user123",
                note = "Dinner",
                distributedExpense = emptyMap()
            )
        )

        val expenseId = "id"
        val mockDocumentSnapshot: DocumentSnapshot = mock {
            on { toObject(FirestoreExpense::class.java) } doReturn expenseList[0]
            on { id } doReturn expenseId
        }

        val mockQuerySnapshot: QuerySnapshot = mock {
            on { documents } doReturn listOf(mockDocumentSnapshot)
        }

        val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.collection(EXPENSES_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.get()).thenReturn(mockTask) // Ensures get() returns mockTask

        // When
        val result = repository.getAllExpensesFromHousehold(householdId)
        advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expenseList.map { it.toDomainExpense(expenseId) }, result.getOrNull())
    }

    @Test
    fun `delete household should call delete`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household456"
        val mockTask: Task<Void> = Tasks.forResult(null)
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.delete()).thenReturn(mockTask)

        // When
        val result = repository.deleteHousehold(userId, householdId)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getExpensesForTimePeriod should return expenses`() = runTest {
        // Given
        val householdId = "household456"
        val expenses = listOf(
            FirestoreExpense(
                total = 100.0,
                category = HOME.displayName,
                timestamp = Timestamp(Date()),
                userId = "user123",
                note = "Dinner",
                distributedExpense = emptyMap()
            )
        )

        val expenseId = "id"
        val mockDocumentSnapshot: DocumentSnapshot = mock {
            on { toObject(FirestoreExpense::class.java) } doReturn expenses[0]
            on { id } doReturn expenseId
        }

        val mockQuerySnapshot: QuerySnapshot = mock {
            on { documents } doReturn listOf(mockDocumentSnapshot)
        }

        val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.collection(EXPENSES_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(mockCollection)
        whenever(mockCollection.whereLessThanOrEqualTo(anyString(), any())).thenReturn(mockCollection)
        whenever(mockCollection.get()).thenReturn(mockTask) // Ensures get() returns mockTask

        // When
        val result = repository.getExpensesForTimePeriod(householdId, Date(), Date())
        advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expenses.map { it.toDomainExpense(expenseId) }, result.getOrNull())
    }

    @Test
    fun `getExpensesForTimePeriod return empty list if no expenses found`() = runTest {
        // Given
        val householdId = "household456"
        val mockQuerySnapshot: QuerySnapshot = mock {
            on { documents } doReturn emptyList()
        }

        val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.collection(EXPENSES_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(
            mockCollection
        )
        whenever(mockCollection.whereLessThanOrEqualTo(anyString(), any())).thenReturn(
            mockCollection
        )
        whenever(mockCollection.get()).thenReturn(mockTask) // Ensures get() returns mockTask

        // When
        val result = repository.getExpensesForTimePeriod(householdId, Date(), Date())
        advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptyList<Expense>(), result.getOrNull())
    }

    @Test
    fun `getExpensesForTimePeriod should return failure on exception`() = runTest {
        // Given
        val householdId = "household456"
        val firebaseException: FirebaseFirestoreException = mock()
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.collection(EXPENSES_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(mockCollection)
        whenever(mockCollection.whereLessThanOrEqualTo(anyString(), any())).thenReturn(mockCollection)
        whenever(mockCollection.get()).thenReturn(Tasks.forException(firebaseException))

        // When
        val result = repository.getExpensesForTimePeriod(householdId, Date(), Date())
        advanceUntilIdle()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `when deleteExpense is called then it should call firestore delete`() = runTest {
        // Given
        val householdId = "household456"
        val expenseId = "expense123"
        val mockTask: Task<Void> = Tasks.forResult(null)
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.collection(EXPENSES_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(expenseId)).thenReturn(mockDocument)
        whenever(mockDocument.delete()).thenReturn(mockTask)

        // When
        val result = repository.deleteExpenseFromHousehold(householdId, expenseId)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when deleteExpense throws exception then result is failure`() = runTest {
        // Given
        val householdId = "household456"
        val expenseId = "expense123"
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        val mockFirebaseException: FirebaseFirestoreException = mock()
        whenever(mockCollection.document(householdId)).then { throw mockFirebaseException }

        // When
        val result = repository.deleteExpenseFromHousehold(householdId, expenseId)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `getHouseholdDetails returns householdDetails`() = runTest {
        // Given
        val householdId = "household123"
        val householdName = "Test Household"
        val usersIds = listOf("user123", "user456")
        val firestoreHousehold = FirestoreHousehold(householdId, householdName, usersIds)
        val mockDocumentSnapshot: DocumentSnapshot = mock {
            on { toObject(FirestoreHousehold::class.java) } doReturn firestoreHousehold
        }
        val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.get()).thenReturn(mockTask)

        // When
        val result = repository.getHouseholdDetails(householdId)

        // Then
        assertTrue(result.isSuccess)
        val householdDetails = result.getOrNull()
        assertEquals(householdId, householdDetails?.id)
        assertEquals(householdName, householdDetails?.name)
        assertEquals(usersIds, householdDetails?.usersIds)
    }

    @Test
    fun `getHouseholdDetails returns failure on exception`() = runTest {
        // Given
        val householdId = "household123"
        val firebaseException: FirebaseFirestoreException = mock()
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.get()).thenReturn(Tasks.forException(firebaseException))

        // When
        val result = repository.getHouseholdDetails(householdId)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `removeUserFromHousehold should call update with new empty list for 1 user`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household456"
        val docSnapshot: DocumentSnapshot = mock()
        val mockTask: Task<DocumentSnapshot> = Tasks.forResult(docSnapshot)
        val mockHousehold: FirestoreHousehold = mock()
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.get()).thenReturn(mockTask)
        whenever(docSnapshot.toObject(FirestoreHousehold::class.java)).thenReturn(mockHousehold)
        whenever(mockHousehold.users).thenReturn(List(1) { userId })
        val mockUpdateTask: Task<Void> = Tasks.forResult(null)
        whenever(mockDocument.update(anyString(), any())).thenReturn(mockUpdateTask)

        // When
        repository.removeUserFromHousehold(userId, householdId)

        // Then
        verify(mockDocument).update(USERS_FIELD, emptyList<String>())
    }

    @Test
    fun `removeUserFromHousehold should call update with rest of the users`() = runTest {
        // Given
        val userId = "user123"
        val someOtherUser = "otherUserId"
        val householdId = "household456"
        val docSnapshot: DocumentSnapshot = mock()
        val mockTask: Task<DocumentSnapshot> = Tasks.forResult(docSnapshot)
        val mockHousehold: FirestoreHousehold = mock()
        whenever(mockFirestore.collection(HOUSEHOLD_COLLECTION)).thenReturn(mockCollection)
        whenever(mockCollection.document(householdId)).thenReturn(mockDocument)
        whenever(mockDocument.get()).thenReturn(mockTask)
        whenever(docSnapshot.toObject(FirestoreHousehold::class.java)).thenReturn(mockHousehold)
        whenever(mockHousehold.users).thenReturn(listOf(userId, someOtherUser))
        val mockUpdateTask: Task<Void> = Tasks.forResult(null)
        whenever(mockDocument.update(anyString(), any())).thenReturn(mockUpdateTask)

        // When
        repository.removeUserFromHousehold(userId, householdId)

        // Then
        verify(mockDocument).update(USERS_FIELD, listOf(someOtherUser))
    }
}
