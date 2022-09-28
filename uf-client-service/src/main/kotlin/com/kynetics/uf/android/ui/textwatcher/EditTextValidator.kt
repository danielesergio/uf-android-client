/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.ui.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

abstract class EditTextValidator(private val editText: EditText) : TextWatcher {

    abstract fun validate(text: String):Result<Unit>

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    final override fun afterTextChanged(s: Editable?) {
        validate(s?.toString() ?: "")
            .onFailure { error ->
                editText.error = error.message
            }
    }
}