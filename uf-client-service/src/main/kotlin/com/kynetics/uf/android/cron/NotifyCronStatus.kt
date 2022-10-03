package com.kynetics.uf.android.cron

import com.kynetics.uf.android.api.v1.UFServiceMessageV1
import com.kynetics.uf.android.communication.messenger.MessengerHandler

fun CronScheduler.Status.notifyScheduleStatus(){
    when(this){
        is CronScheduler.Status.Scheduled ->
            MessengerHandler.onAndroidMessage(UFServiceMessageV1.State.WaitingUpdateWindow(this.seconds))
        is CronScheduler.Status.Error ->{
            MessengerHandler.onAndroidMessage(UFServiceMessageV1.State.WaitingUpdateWindow(-1))
            MessengerHandler.onAndroidMessage(UFServiceMessageV1.Event.Error(this.details))
        }
    }
}