/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.kynetics.uf.android.content

import android.content.SharedPreferences
import java.io.Serializable

interface SharedPreferencesWithObject:SharedPreferences {
    fun <T : Serializable?> getObject(objKey: String?): T?

    fun <T : Serializable?> getObject(objKey: String?, defaultObj: T?): T?

    fun <T> putAndCommitObject(key: String?, obj: T)
}