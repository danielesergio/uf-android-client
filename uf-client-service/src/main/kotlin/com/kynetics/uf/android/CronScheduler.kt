package com.kynetics.uf.android

import com.cronutils.model.time.ExecutionTime
import kotlinx.coroutines.*
import java.time.LocalTime
import java.time.ZonedDateTime

@OptIn(DelicateCoroutinesApi::class)
object CronScheduler {

    private var authJob: Job? = null

    fun schedule(expression: String, authorize:()->Unit){
        with(ExecutionTime.forCron(HaraCronParser.parse(expression))){
            val now: ZonedDateTime = ZonedDateTime.now()
            val nextExecution = nextExecution(now).get()
            val lastExecution = lastExecution(now).get()
            authJob?.cancel()
            if(isNowOnValidTimeWindow(lastExecution.toLocalTime(), nextExecution.toLocalTime())){
                authorize()
            }else{
                authJob = GlobalScope.launch(Dispatchers.IO) {
                    val triggerMillis = nextExecution.toInstant().toEpochMilli().minus(now.toInstant().toEpochMilli())
                    delay(triggerMillis)
                    authorize()
                }
            }
        }
    }

    private fun isNowOnValidTimeWindow(last: LocalTime, next: LocalTime) : Boolean{
        return LocalTime.now().isAfter(last) && LocalTime.now().isBefore(next)
    }
}