package com.kynetics.uf.android.configuration

import com.kynetics.uf.android.cron.CronScheduler
import com.kynetics.uf.android.api.UFServiceConfigurationV2
import com.kynetics.uf.android.api.v1.UFServiceMessageV1
import com.kynetics.uf.android.communication.messenger.MessengerHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.eclipse.hara.ddiclient.api.DeploymentPermitProvider

interface AndroidForceDeploymentPermitProvider: DeploymentPermitProvider {

    companion object{

        fun build(tag:String, timeWindows: UFServiceConfigurationV2.TimeWindows):AndroidForceDeploymentPermitProvider{
            return object : AndroidForceDeploymentPermitProvider{

                private var forceResponse = CompletableDeferred<Boolean>()

                override fun updateAllowed(): Deferred<Boolean> {
                    forceResponse.complete(false)
                    forceResponse = CompletableDeferred()

                    CronScheduler.schedule(tag, timeWindows){
                        forceResponse.complete(true)
                    }.also { status ->
                        when(status){
                            is CronScheduler.Status.Scheduled ->
                                MessengerHandler.onAndroidMessage(UFServiceMessageV1.State.WaitingUpdateWindow(status.seconds))
                            is CronScheduler.Status.Error ->{
                                MessengerHandler.onAndroidMessage(UFServiceMessageV1.State.WaitingUpdateWindow(-1))
                                MessengerHandler.onAndroidMessage(UFServiceMessageV1.Event.Error(status.details))
                            }
                        }
                    }

                    return forceResponse
                }
            }
        }
    }
}