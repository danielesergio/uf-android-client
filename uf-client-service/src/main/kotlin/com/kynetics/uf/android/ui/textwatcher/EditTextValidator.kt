package com.kynetics.uf.android.ui.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

abstract class EditTextValidator(private val editText: EditText) : TextWatcher {

    abstract fun validate(text: String):Result<Unit>

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    final override fun afterTextChanged(s: Editable?) {
        val text = editText.text.toString()
        validate(text)
            .onFailure { error ->
                editText.error = error.message
            }
    }
}