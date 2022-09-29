/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Suppress("KDocUnresolvedReference")
@Serializable
/**
 * This class represent the [com.kynetics.uf.android.UpdateFactoryService]'s configuration
 *
 * @property tenant the tenant
 * @property controllerId id of the controller
 * @property url of update server
 * @property targetToken target token
 * @property gatewayToken gateway token
 * @property isApiMode true to handle authorization via service messages
 *  [com.kynetics.uf.android.api.Communication.V1.In.AuthorizationResponse], false to use dialog
 * @property isEnable true to enable the service, false to disable it
 * @property isUpdateFactoryServe true when the server is an UpdateServer, false when the server is an
 *  Hawkbit server
 * @property targetAttributes target metadata sent to the server
 * @property updateWindows configuration parameters for time-windows updates
 */
data class UFServiceConfigurationV2(
    /**
     * The tenant
     */
    val tenant: String,

    /**
     * Id of the controller
     */
    val controllerId: String,

    /**
     * the update server url
     */
    val url: String,

    /**
     * Target token
     */
    val targetToken: String,

    /**
     * Gateway token
     */
    val gatewayToken: String,

    /**
     * true to handle authorization via service messages, false to use dialog
     *
     * @see com.kynetics.uf.android.api.Communication.V1.In.AuthorizationResponse
     */
    val isApiMode: Boolean,

    /**
     * true to enable the service, false otherwise
     */
    val isEnable: Boolean,

    /**
     * true when the server is an UpdateServer, false when the server is an Hawkbit server
     */
    val isUpdateFactoryServe: Boolean,

    /**
     * target metadata sent to the server
     */
    val targetAttributes: Map<String, String> = emptyMap(),

    /**
     * configuration parameters for time-windows updates
     */
    val updateWindows: TimeWindows = TimeWindows()
){
    @Serializable
    /**
     * Class to configure the force update behaviour. It defines when a force update can be applied
     * based on a schedule defined as cron expression (QUARTZ) and a number of seconds.
     *
     * @property cronExpression, a cron expression (QUARTZ) that defines the update windows
     *  beginning time.
     * @property windowSize, duration of update windows in seconds. Min value is 1.
     */
    data class TimeWindows(
        /**
         * A cron expression (QUARTZ) that defines the update windows beginning time.
         * Default value: * * * ? * *
         */
        val cronExpression:String = ALWAYS,
        /**
         * Duration of update windows in seconds. Min value is 1.
         * Default value: 3600
         */
        val windowSize: Long = DEFAULT_WINDOW_SIZE
    ) {
        companion object{
            const val ALWAYS:String = "* * * ? * *"
            const val DEFAULT_WINDOW_SIZE: Long = 3600
        }
        val isValid:Boolean = windowSize > 1
    }

    /**
     * True if the configuration is valid
     */
    val isValid:Boolean by lazy{
        tenant.isNotEmpty() &&
                url.isNotEmpty() &&
                controllerId.isNotEmpty() &&
                (targetToken.isNotEmpty() || gatewayToken.isNotEmpty()) &&
                updateWindows.isValid
    }

    /**
     * Json serialization
     */
    fun toJson(): String {
        return json.encodeToString(serializer(), this)
    }

    companion object{
        private val json = Json { encodeDefaults = true }

        /**
         * Deserializes given json [data] into a corresponding object of type [UFServiceConfigurationV2].
         * @throws [JsonException] in case of malformed json
         * @throws [SerializationException] if given input can not be deserialized
         * @return the [UFServiceConfigurationV2] parsed from [data]
         */
        @JvmStatic
        fun fromJson(data: String): UFServiceConfigurationV2 {
            return json.decodeFromString(serializer(), data)
        }
    }
}