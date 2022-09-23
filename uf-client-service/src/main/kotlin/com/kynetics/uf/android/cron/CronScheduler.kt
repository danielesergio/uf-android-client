/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.cron

import android.util.Log
import com.cronutils.model.time.ExecutionTime
import com.kynetics.uf.android.api.UFServiceConfigurationV2
import kotlinx.coroutines.*
import java.time.ZonedDateTime
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
object CronScheduler {
    private val TAG:String = CronScheduler::class.java.simpleName

    private var scheduledJobs: MutableMap<String, Job> = mutableMapOf()

    sealed interface Status{
        data class Error(val details:List<String>):Status
        data class Scheduled(val seconds:Long):Status
    }

    fun schedule(tag:String,
                 timeWindows: UFServiceConfigurationV2.TimeWindows,
                 action:()->Unit):Status{
        with(ExecutionTime.forCron(HaraCronParser.parse(timeWindows.cronExpression))){
            val now: ZonedDateTime = ZonedDateTime.now()
            val lastExecution = lastExecution(now)
            val nextExecution = timeToNextExecution(now)
            scheduledJobs[tag]?.cancel()
            return when{
                isInTimeWindows(now, lastExecution, timeWindows) ->{
                    action()
                    Status.Scheduled(0)
                }

                nextExecution.isPresent -> {
                    runCatching{
                        val delay = nextExecution.get().toMillis()
                        scheduledJobs[tag] = GlobalScope.launch(Dispatchers.IO) {
                            delay(delay)
                            action()
                        }
                        Status.Scheduled(nextExecution.get().seconds)
                    }.onFailure { exception ->
                        Log.w(TAG, "Error on scheduling next job", exception)
                        return Status.Error(listOf("Error on scheduling next job", exception.message?:""))
                    }.getOrThrow()
                }

                else -> {
                    Log.w(TAG, "Next execution not exist")
                    Status.Error(listOf("Next update windows not exist", "Check the cron expression"))
                }
            }
        }
    }

    fun removeScheduledJob(tag:String) = scheduledJobs[tag]?.cancel()

    private fun isInTimeWindows(now:ZonedDateTime,
                                lastExecution: Optional<ZonedDateTime>,
                                timeWindows: UFServiceConfigurationV2.TimeWindows):Boolean{
        return lastExecution.isPresent &&
                now.isAfter(lastExecution.get()) &&
                now.isBefore(lastExecution.get().plusSeconds(timeWindows.windowSize))
    }
}