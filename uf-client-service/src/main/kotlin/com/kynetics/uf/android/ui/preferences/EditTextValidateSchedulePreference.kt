package com.kynetics.uf.android.ui.preferences

import android.content.Context
import android.util.AttributeSet
import com.kynetics.uf.android.HaraCronParser

class EditTextValidateSchedulePreference(
    context: Context,
    attrs: AttributeSet
) : EditTextValidatePreference(context, attrs) {

    override fun validateText(text: String): Result<Unit> = runCatching { HaraCronParser.parse(text) }

}