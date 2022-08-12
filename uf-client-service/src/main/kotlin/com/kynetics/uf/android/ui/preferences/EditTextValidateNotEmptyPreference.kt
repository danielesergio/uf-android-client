package com.kynetics.uf.android.ui.preferences

import android.content.Context
import android.util.AttributeSet

class EditTextValidateNotEmptyPreference(
    context: Context,
    attrs: AttributeSet
) : EditTextValidatePreference(context, attrs) {

    override fun validateText(text: String): Result<Unit> {
        return if (text.trim { it <= ' ' } == "") {
            Result.failure(IllegalArgumentException("Filed can't be empty"))
        } else {
            Result.success(Unit)
        }
    }
}