package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import javax.inject.Inject

class NotificationsTokenUpdateUseCase @Inject constructor(
    private val usersCollectionRepository: UsersCollectionRepository,
    private val userSessionRepository: UserSessionRepository
) {

    suspend fun onNewNotificationToken(newToken: String) {
        userSessionRepository.getUser()?.id?.let { userId ->
            val updateResult = usersCollectionRepository.updateNotificationToken(userId, newToken)
            updateResult.fold(
                onSuccess = { userSessionRepository.updateNotificationToken(newToken)},
                onFailure = { /* Do nothing */ }
            )
        }
    }
}
