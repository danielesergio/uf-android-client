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

@Serializable
data class UFServiceConfigurationV2(
    val tenant: String,
    val controllerId: String,
    val url: String,
    val targetToken: String,
    val gatewayToken: String,
    val isApiMode: Boolean,
    val isEnable: Boolean,
    val isUpdateFactoryServe: Boolean,
    val targetAttributes: Map<String, String> = emptyMap(),
    val updateWindows: TimeWindows = TimeWindows()
){
    @Serializable
    data class TimeWindows(
        val cronExpression:String = ALWAYS,
        val windowSize: Long = DEFAULT_WINDOW_SIZE
    ) {
        companion object{
            const val ALWAYS:String = "* * * ? * *"
            const val DEFAULT_WINDOW_SIZE: Long = 3600
        }
        val isValid:Boolean by lazy{
            true
        }
    }

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
         */
        @JvmStatic
        fun fromJson(data: String): UFServiceConfigurationV2 {
            return json.decodeFromString(serializer(), data)
        }
    }
}