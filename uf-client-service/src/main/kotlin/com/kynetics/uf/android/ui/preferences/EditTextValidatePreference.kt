package com.kynetics.uf.android.ui.preferences

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference.OnPreferenceChangeListener
import com.kynetics.uf.android.ui.textwatcher.EditTextValidator

abstract class EditTextValidatePreference(
    context: Context,
    attrs: AttributeSet) : EditTextPreference(context, attrs) {

    abstract fun validateText(text: String) : Result<Unit>

    init{
        initialization()
    }

    private fun initialization(){
        setOnBindEditTextListener { editText ->
            with(editText) {
                inputType = InputType.TYPE_CLASS_TEXT
                addTextChangedListener(object : EditTextValidator(this){
                    override fun validate(text: String): Result<Unit> = validateText(text)
                })
            }
        }
        onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
            validateText(newValue.toString()).onFailure {
                Toast.makeText(context, "Not updated: " + it.message,
                    Toast.LENGTH_LONG).show()
            }.isSuccess
        }
    }

}