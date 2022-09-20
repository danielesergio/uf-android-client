package com.kynetics.uf.android.configuration

import com.kynetics.uf.android.cron.CronScheduler
import com.kynetics.uf.android.api.UFServiceConfigurationV2
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.eclipse.hara.ddiclient.api.DeploymentPermitProvider

interface AndroidForceDeploymentPermitProvider: DeploymentPermitProvider {

    companion object{

        fun build(timeWindows: UFServiceConfigurationV2.TimeWindows):AndroidForceDeploymentPermitProvider{
            return object : AndroidForceDeploymentPermitProvider{

                private var forceResponse = CompletableDeferred<Boolean>()

                override fun updateAllowed(): Deferred<Boolean> {
                    forceResponse.complete(false)
                    forceResponse = CompletableDeferred()

                    CronScheduler.schedule(timeWindows){
                        forceResponse.complete(true)
                    }

                    return forceResponse
                }
            }
        }
    }
}