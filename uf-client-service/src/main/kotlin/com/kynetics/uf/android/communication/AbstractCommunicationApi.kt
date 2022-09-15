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
import android.os.Messenger
import android.util.Log
import com.kynetics.uf.android.api.ApiCommunicationVersion
import com.kynetics.uf.android.api.Communication
import com.kynetics.uf.android.api.UFServiceConfiguration
import com.kynetics.uf.android.configuration.ConfigurationHandler
import com.kynetics.uf.android.api.Communication.Companion.SERVICE_API_VERSION_KEY
import com.kynetics.uf.android.client.RestartableClientService
import com.kynetics.uf.android.configuration.AndroidDeploymentPermitProvider

abstract class AbstractCommunicationApi(
    private val configurationHandler: ConfigurationHandler,
    private val ufService:RestartableClientService,
    private val softDeploymentPermitProvider: AndroidDeploymentPermitProvider
):GenericCommunicationApi {

    protected abstract val api: ApiCommunicationVersion
    //        val api = ApiCommunicationVersion.fromVersionCode(msg.data.getInt(SERVICE_API_VERSION_KEY, 0))
    companion object{
        val TAG:String = AbstractCommunicationApi::class.java.simpleName
    }

    override fun subscribeClient(messenger: Messenger?, apiVersion: ApiCommunicationVersion) {
        MessengerHandler.subscribeClient(messenger, apiVersion)
    }

    override fun unsubscribeClient(messenger: Messenger?) {
        MessengerHandler.unsubscribeClient(messenger)
    }

    override fun sync(messenger: Messenger?) {
        Log.i(TAG, "received sync request")

        if (messenger == null) {
            Log.i(TAG, "command ignored because field replyTo is null")
            return
        }

        MessengerHandler.response(
            configurationHandler.getCurrentConfiguration(),
            Communication.V1.Out.CurrentServiceConfiguration.ID,
            messenger
        )

        if (MessengerHandler.hasMessage(api)) {
            MessengerHandler.response(
                MessengerHandler.getlastSharedMessage(api).messageToSendOnSync,
                Communication.V1.Out.ServiceNotification.ID,
                messenger
            )
        }
        Log.i(TAG, "client synced")
    }

    override fun forcePing() {
        ufService.forcePing()
    }

    override fun configureService(newConf: UFServiceConfiguration) {
        val currentConf = configurationHandler.getCurrentConfiguration()

        if (currentConf != newConf) {
            configurationHandler.saveServiceConfigurationToSharedPreferences(newConf)
            Log.i(TAG, "configuration updated")
        } else {
            Log.i(TAG, "new configuration equals to current configuration")
        }

        if (configurationHandler.needReboot(currentConf)) {
            ufService.restartService(configurationHandler)
            Log.i(TAG, "configuration updated - restarting service")
        } else {
            Log.i(TAG, "configuration updated - service not restarted")
        }
    }

    override fun authorizationResponse(msg: Message) {
        Log.i(TAG, "receive authorization response")
        if(!msg.data.containsKey(Communication.V1.SERVICE_DATA_KEY)){
            Log.i(TAG, "Invalid authorization response message received")
            return
        }
        val response = msg.data.getBoolean(Communication.V1.SERVICE_DATA_KEY)
        softDeploymentPermitProvider.allow(response)

        Log.i(TAG, String.format("authorization %s", if (response) "granted" else "denied"))
    }

    override fun onMessage(msg: Message) {
        when (msg.what) {
            Communication.V1.In.ConfigureService.ID -> configureService(msg)

            Communication.V1.In.RegisterClient.ID -> {
                Log.i(TAG, "receive subscription request")
                subscribeClient(msg.replyTo, ApiCommunicationVersion.fromVersionCode(msg.data.getInt(
                    SERVICE_API_VERSION_KEY, 0)))
            }

            Communication.V1.In.UnregisterClient.ID -> {
                Log.i(TAG, "receive un-subscription request")
                unsubscribeClient(msg.replyTo)
            }

            Communication.V1.In.AuthorizationResponse.ID -> authorizationResponse(msg)

            Communication.V1.In.ForcePing.id -> {
                Log.i(TAG, "receive request to resume suspend state")
                forcePing()
            }

            Communication.V1.In.Sync.ID -> sync(msg.replyTo)

            else -> Log.i(TAG, "Invalid message receive (what == ${msg.what})")
        }
    }

    private fun configureService(msg: Message) {
        Log.i(TAG, "receive configuration update request")
        val configuration =  try{
            if(!msg.data.containsKey(Communication.V1.SERVICE_DATA_KEY)){
                Log.i(TAG, "Invalid configuration message received (no configuration found)")
                return
            }
            msg.data.getSerializable(Communication.V1.SERVICE_DATA_KEY) as UFServiceConfiguration
        } catch (e:Throwable){
            Log.i(TAG, "Invalid configuration message received; Error on configuration deserialize.")
            return
        }
        configureService(configuration)
    }
}