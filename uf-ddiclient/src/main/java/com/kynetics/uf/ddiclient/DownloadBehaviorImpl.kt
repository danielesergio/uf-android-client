/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.ddiclient

import org.eclipse.hara.ddiclient.api.DownloadBehavior
import kotlin.math.pow

class DownloadBehaviorImpl(
    private val maxDelay:Long = 3600,
    private val minDelay:Long = 10,
    private val maxAttempts:Int = 36
): DownloadBehavior {

    override fun onAttempt(attempt: Int, artifactId:String, previousError: Throwable?): DownloadBehavior.Try {
        return when{
            attempt == 1 -> DownloadBehavior.Try.After(0)

            attempt > maxAttempts -> DownloadBehavior.Try.Stop

            else -> 2.0.pow(attempt.toDouble()).toLong().coerceIn(minDelay, maxDelay)
                .run { DownloadBehavior.Try.After(this) }
        }

    }

}