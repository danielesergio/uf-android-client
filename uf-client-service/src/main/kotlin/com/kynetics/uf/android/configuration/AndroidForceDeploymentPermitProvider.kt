/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.configuration

import com.kynetics.uf.android.cron.CronScheduler
import com.kynetics.uf.android.api.UFServiceConfigurationV2
import com.kynetics.uf.android.cron.notifyScheduleStatus
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
                    }.notifyScheduleStatus()

                    return forceResponse
                }
            }
        }
    }
}