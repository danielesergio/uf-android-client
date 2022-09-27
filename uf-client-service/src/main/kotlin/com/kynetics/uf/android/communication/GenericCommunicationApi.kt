/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

@file:Suppress("ClassName", "DEPRECATION")

package com.kynetics.uf.android.communication

import android.os.Message
import android.os.Messenger
import com.kynetics.uf.android.api.ApiCommunicationVersion
import com.kynetics.uf.android.api.UFServiceConfiguration

interface GenericCommunicationApi:CommunicationApi {
    fun subscribeClient(messenger: Messenger?, apiVersion: ApiCommunicationVersion)
    fun unsubscribeClient(messenger: Messenger?)
    fun sync(messenger: Messenger?)
    fun forcePing()
    fun configureService(newConf:UFServiceConfiguration)
    fun authorizationResponse(msg:Message)
}