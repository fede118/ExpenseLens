package com.section11.expenselens.ui.home.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfoUiModel(
    val id: String,
    val displayName: String?,
    val profilePic: String?
): Parcelable
