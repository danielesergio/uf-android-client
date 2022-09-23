package com.kynetics.uf.android.ui.preferences

import android.content.Context
import android.util.AttributeSet

class NumbersPreference(
    context: Context,
    attrs: AttributeSet
) : EditTextValidatePreference(context, attrs) {

    override fun validateText(text: String): Result<Unit> {
        return runCatching {
            text.toLong()
        }
    }
}