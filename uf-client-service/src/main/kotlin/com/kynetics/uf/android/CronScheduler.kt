package com.kynetics.uf.android

import com.cronutils.model.time.ExecutionTime
import com.kynetics.uf.android.api.UFServiceConfiguration
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

@OptIn(DelicateCoroutinesApi::class)
object CronScheduler {

    private var authJob: Job? = null

    fun schedule(timeWindows: UFServiceConfiguration.TimeWindows, action:()->Unit){
        with(ExecutionTime.forCron(HaraCronParser.parse(timeWindows.cronExpression))){
            val now: ZonedDateTime = ZonedDateTime.now()
            val nextExecution = nextExecution(now).get()
            val lastExecution = lastExecution(now).get()
            authJob?.cancel()
            if(isNowOnValidTimeWindow(lastExecution.toLocalTime(), lastExecution.plusSeconds(timeWindows.windowSize).toLocalTime())){
                action()
            }else{
                authJob = GlobalScope.launch(Dispatchers.IO) {
                    delay(Duration.between(now, nextExecution).toMillis())
                    action()
                }
            }
        }
    }

    private fun isNowOnValidTimeWindow(last: LocalTime, next: LocalTime) : Boolean{
        return LocalTime.now().isAfter(last) && LocalTime.now().isBefore(next)
    }
}