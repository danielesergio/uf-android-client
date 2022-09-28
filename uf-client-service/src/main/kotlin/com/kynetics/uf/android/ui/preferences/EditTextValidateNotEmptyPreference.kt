/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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