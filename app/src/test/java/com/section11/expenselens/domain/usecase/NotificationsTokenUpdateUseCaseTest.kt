package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import com.section11.expenselens.ui.utils.getUserData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class NotificationsTokenUpdateUseCaseTest {
    private val usersCollectionRepository: UsersCollectionRepository = mock()
    private val userSessionRepository: UserSessionRepository = mock()
    private val notificationsTokenUpdateUseCase = NotificationsTokenUpdateUseCase(
        usersCollectionRepository,
        userSessionRepository
    )

    @Test
    fun `onNewNotificationToken should update token when user is present`() = runTest {
        // Given
        val newToken = "newToken"
        val user = getUserData()
        whenever(userSessionRepository.getUser()).thenReturn(user)
        whenever(usersCollectionRepository.updateNotificationToken(user.id, newToken))
            .thenReturn(Result.success(Unit))

        // When
        notificationsTokenUpdateUseCase.onNewNotificationToken(newToken)

        // Then
        verify(usersCollectionRepository).updateNotificationToken(user.id, newToken)
        verify(userSessionRepository).updateNotificationToken(newToken)
    }

    @Test
    fun `onNewNotificationToken should do nothing when user is not present`() = runTest {
        // Given
        val newToken = "newToken"
        whenever(userSessionRepository.getUser()).thenReturn(null)

        // When
        notificationsTokenUpdateUseCase.onNewNotificationToken(newToken)

        // Then
        verify(usersCollectionRepository, never()).updateNotificationToken(anyString(), anyString())
        verify(userSessionRepository, never()).updateNotificationToken(anyString())
    }

    @Test
    fun `onNewNotificationToken should handle failure when updating token`() = runTest {
        // Given
        val newToken = "newToken"
        val user = getUserData()
        whenever(userSessionRepository.getUser()).thenReturn(user)
        whenever(usersCollectionRepository.updateNotificationToken(user.id, newToken))
            .thenReturn(Result.failure(Exception()))

        // When
        notificationsTokenUpdateUseCase.onNewNotificationToken(newToken)

        // Then
        verify(usersCollectionRepository).updateNotificationToken(user.id, newToken)
        verify(userSessionRepository, never()).updateNotificationToken(newToken)
    }
}
