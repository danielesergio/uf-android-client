/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.communication

import android.os.Message
import android.util.Log
import com.kynetics.uf.android.api.ApiCommunicationVersion
import com.kynetics.uf.android.api.Communication.Companion.SERVICE_API_VERSION_KEY
import com.kynetics.uf.android.client.RestartableClientService
import com.kynetics.uf.android.configuration.AndroidDeploymentPermitProvider
import com.kynetics.uf.android.configuration.AndroidForceDeploymentPermitProvider
import com.kynetics.uf.android.configuration.ConfigurationHandler
import java.util.*

class CommunicationFacade private constructor(
    private val communicationApis: Map<ApiCommunicationVersion, CommunicationApi>
):CommunicationApi {


    override fun onMessage(msg: Message) {
        runCatching {
            val api = ApiCommunicationVersion.fromVersionCode(msg.getApiVersionCode())
            communicationApis.getValue(api).onMessage(msg)
        }.onFailure { e ->
            Log.w(TAG, "unsupported communication version used by the client (api: ${msg.getApiVersionCode()}", e)
        }


    }

    private fun Message.getApiVersionCode():Int = data.getInt(SERVICE_API_VERSION_KEY, 0)

    companion object{
        private val TAG:String = CommunicationFacade::class.java.simpleName

        fun newInstance(
            configurationHandler: ConfigurationHandler,
            restartableClientService: RestartableClientService,
            softDeploymentPermitProvider: AndroidDeploymentPermitProvider
        ) :CommunicationFacade{
            return CommunicationFacade(
                mapOf(
                    ApiCommunicationVersion.V0_1 to CommunicationApiV0_1Impl(
                        configurationHandler,
                        restartableClientService,
                        softDeploymentPermitProvider
                    ),

                    ApiCommunicationVersion.V1 to CommunicationApiV0_1Impl(
                        configurationHandler,
                        restartableClientService,
                        softDeploymentPermitProvider
                    )
                )
            )
        }
    }

}