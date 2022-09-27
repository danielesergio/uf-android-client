/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.kynetics.uf.android.update.application

import java.io.File
import java.util.concurrent.TimeUnit

interface UpdateConfirmationTimeoutProvider {
    class Timeout(val value: Long, val timeUnit: TimeUnit)

    fun getTimeout(files: List<File?>?): Timeout
    class FixedTimeProvider private constructor(private val timeout: Long) :
        UpdateConfirmationTimeoutProvider {
        override fun getTimeout(files: List<File?>?): Timeout {
            return Timeout(timeout, TimeUnit.SECONDS)
        }

        companion object {
            fun ofSeconds(seconds: Long): UpdateConfirmationTimeoutProvider {
                return FixedTimeProvider(seconds)
            }
        }

    }
}