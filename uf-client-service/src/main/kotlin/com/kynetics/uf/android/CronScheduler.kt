package com.kynetics.uf.android

import android.util.Log
import com.cronutils.model.time.ExecutionTime
import com.kynetics.uf.android.api.UFServiceConfigurationV2
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

@OptIn(DelicateCoroutinesApi::class)
object CronScheduler {
    private val TAG:String = CronScheduler::class.java.simpleName

    private var authJob: Job? = null

    fun schedule(timeWindows: UFServiceConfigurationV2.TimeWindows, action:()->Unit){
        with(ExecutionTime.forCron(HaraCronParser.parse(timeWindows.cronExpression))){
            val now: ZonedDateTime = ZonedDateTime.now()
            val nextExecution = nextExecution(now)
            val lastExecution = lastExecution(now)
            authJob?.cancel()
            when{
                lastExecution.isPresent &&
                        isNowOnValidTimeWindow(
                            now.toLocalTime(),
                            lastExecution.get().toLocalTime(),
                            lastExecution.get().plusSeconds(timeWindows.windowSize).toLocalTime()) ->{
                                action()
                            }
                nextExecution.isPresent -> {
                    authJob = GlobalScope.launch(Dispatchers.IO) {
                        delay(Duration.between(now, nextExecution.get()).toMillis())
                        action()
                    }
                }
                else -> {
                    Log.w(TAG, "Next execution not exist")
                }
            }
        }
    }

    private fun isNowOnValidTimeWindow(now:LocalTime, last: LocalTime, next: LocalTime) : Boolean{
        return now.isAfter(last) && now.isBefore(next)
    }
}