package com.kynetics.uf.android.configuration

import com.kynetics.uf.android.CronScheduler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.eclipse.hara.ddiclient.api.DeploymentPermitProvider

interface AndroidForceDeploymentPermitProvider: DeploymentPermitProvider {

    companion object{

        fun build(configurationHandler: ConfigurationHandler):AndroidForceDeploymentPermitProvider{
            return object : AndroidForceDeploymentPermitProvider{

                private var forceResponse = CompletableDeferred<Boolean>()

                override fun updateAllowed(): Deferred<Boolean> {
                    forceResponse.complete(false)
                    forceResponse = CompletableDeferred()

                    val cronExpression = configurationHandler.getScheduleUpdate()

                    CronScheduler.schedule(cronExpression!!){
                        forceResponse.complete(true)
                    }

                    return forceResponse
                }
            }
        }
    }
}