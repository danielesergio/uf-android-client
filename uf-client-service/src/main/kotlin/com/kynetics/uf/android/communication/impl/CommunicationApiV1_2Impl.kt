/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.communication.impl

import android.os.Message
import com.kynetics.uf.android.api.ApiCommunicationVersion
import com.kynetics.uf.android.api.Communication
import com.kynetics.uf.android.client.RestartableClientService
import com.kynetics.uf.android.communication.CommunicationApiV1_2
import com.kynetics.uf.android.configuration.AndroidDeploymentPermitProvider
import com.kynetics.uf.android.configuration.ConfigurationHandler

@Suppress("ClassName")
class CommunicationApiV1_2Impl(configurationHandler: ConfigurationHandler,
                               ufService: RestartableClientService,
                               softDeploymentPermitProvider: AndroidDeploymentPermitProvider
):
    CommunicationApiV1_1Impl(configurationHandler, ufService, softDeploymentPermitProvider),
    CommunicationApiV1_2 {

    override val api: ApiCommunicationVersion = ApiCommunicationVersion.V1_1

    override fun onMessage(msg: Message) {
        @Suppress("UNCHECKED_CAST")
        when (msg.what) {
            Communication.V1.In.AddTargetAttributes.ID -> addTargetAttributes(msg.data.getSerializable(Communication.V1.SERVICE_DATA_KEY) as HashMap<String,String>)
            else -> super.onMessage(msg)
        }
    }

    override fun addTargetAttributes(targetAttributes: Map<String, String>) {
        configurationHandler.saveAddTargetAttributes(targetAttributes)
    }

    companion object{
        val TAG:String = CommunicationApiV1_2Impl::class.java.simpleName

    }
}