/*
 * Copyright © 2017-2020  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.api

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import com.kynetics.uf.android.api.v1.UFServiceMessageV1

/**
 * Transform an instance of [android.os.Message] to [Communication.V1.Out]
 *
 * @throws IllegalArgumentException if the message can't be transformed to an
 *   [Communication.V1.Out] instance.
 */
@Suppress("DEPRECATION")
fun Message.toOutV1Message(): Communication.V1.Out {
    return runCatching {
        when (this.what) {
            Communication.V1.Out.ServiceNotification.ID ->
                Communication.V1.Out.ServiceNotification(UFServiceMessageV1.fromJson(data.getString(
                    Communication.V1.SERVICE_DATA_KEY)!!))

            Communication.V1.Out.CurrentServiceConfiguration.ID ->
                Communication.V1.Out.CurrentServiceConfiguration(data.getSerializable(Communication.V1.SERVICE_DATA_KEY
                ) as UFServiceConfiguration)

            Communication.V1.Out.AuthorizationRequest.ID ->
                Communication.V1.Out.AuthorizationRequest(data.getString(
                    Communication.V1.SERVICE_DATA_KEY
                )!!)

            Communication.V1.Out.CurrentServiceConfigurationV2.ID -> Communication.V1.Out.CurrentServiceConfigurationV2(
                UFServiceConfigurationV2.fromJson(data.getString(Communication.V1.SERVICE_DATA_KEY)!!)
            )

            else -> throw IllegalArgumentException("This message isn't sent by UF client (with api v1)")
        }
    }.onFailure {
        throw IllegalArgumentException("This message isn't sent by UF client (with api v1)", it)
    }.getOrThrow()
}

/**
 * Class that maps all messages that are exchanged with the [com.kynetics.uf.android.UpdateFactoryService]
 */
@Suppress("KDocUnresolvedReference")
sealed class Communication(val id: Int) {

    companion object {
        /**
         * [android.os.Bundle]'s key where the ApiCommunicationVersion of the message is stored
         *
         * @property id message code so that the recipient can identify what this message is about
         *  ([android.os.Message.what])
         */
        const val SERVICE_API_VERSION_KEY = "API_VERSION_KEY"
    }

    sealed class V1(id: Int) : Communication(id) {

        companion object {
            /**
             * [android.os.Bundle]'s key where the additional info are stored
             */
            const val SERVICE_DATA_KEY = "DATA_KEY"
        }

        /**
         * Class that maps all the messages that are sent to com.kynetics.uf.android.UpdateFactoryService
         *
         * @property id message code so that the recipient can identify what this message is about
         *  ([android.os.Message.what])
         */
        sealed class In(id: Int) : V1(id) {

            /**
             * Convert the object to the corresponding [android.os.Message] instance.
             */
            open fun toMessage(): Message {
                val msg = Message.obtain(null, id)
                val bundleWithApiVersion = bundle()
                bundleWithApiVersion.putInt(SERVICE_API_VERSION_KEY, ApiCommunicationVersion.V1_1.versionCode)
                msg.data = bundleWithApiVersion
                return msg
            }

            /**
             * Class that maps all messages that are sent to [com.kynetics.uf.android.UpdateFactoryService]
             * that must receive a response.
             *
             * @property replyTo [Messenger] where replies to this message is sent
             * @property id message code so that the recipient can identify what this message is about
             *  ([android.os.Message.what])
             */
            abstract class WithReplyTo(val replyTo: Messenger, id: Int) : In(id) {
                override fun toMessage(): Message {
                    val msg = super.toMessage()
                    msg.replyTo = replyTo
                    return msg
                }
            }

            /**
             * @suppress
             */
            internal open fun bundle(): Bundle = Bundle()

            /**
             *  Class use to build a message to configure the service
             *
             *  @property conf the service configuration
             *  @see UFServiceConfiguration
             */
            @Deprecated("As of release 1.3.0 replaced by com.kynetics.uf.android.api.Communication.V1.In.ConfigureServiceV2")
            @Suppress("DEPRECATION")
            class ConfigureService(@Suppress("MemberVisibilityCanBePrivate") val conf: UFServiceConfiguration) : In(ID) {
                companion object {
                    const val ID = 1
                }

                /**
                 * @suppress
                 */
                override fun bundle(): Bundle {
                    return super.bundle().apply {
                        putSerializable(SERVICE_DATA_KEY, conf)
                    }
                }
            }

            /**
             *  Class use to build a message to configure the service
             *
             *  @property conf the service configuration
             *  @see UFServiceConfigurationV2
             */
            class ConfigureServiceV2(@Suppress("MemberVisibilityCanBePrivate") val conf: UFServiceConfigurationV2) : In(ID) {
                companion object {
                    const val ID = 10
                }

                /**
                 * @suppress
                 */
                override fun bundle(): Bundle {
                    return super.bundle().apply {
                        putSerializable(SERVICE_DATA_KEY, conf.toJson())
                    }
                }
            }

            /**
             * Class use to build a message to subscribe a [Messenger] to the service notification
             * system.
             *
             * @property replyTo the client that it want to subscribe to service notification
             */
            class RegisterClient(replyTo: Messenger) : WithReplyTo(replyTo, ID) {
                companion object {
                    const val ID = 2
                }
            }

            /**
             * Class use to build a message to unsubscribe a [Messenger] to the service notification
             * system.
             *
             * @property replyTo the client that it want to unsubscribe to service notification
             */
            class UnregisterClient(replyTo: Messenger) : WithReplyTo(replyTo, ID) {
                companion object {
                    const val ID = 3
                }
            }

            /**
             * Class use to build a message to grant / denied  an authorization
             *
             */
            class AuthorizationResponse(@Suppress("MemberVisibilityCanBePrivate")  val granted: Boolean) : In(ID) {
                companion object {
                    const val ID = 6
                }

                /**
                 * @suppress
                 */
                override fun bundle(): Bundle {
                    return super.bundle().apply {
                        putBoolean(SERVICE_DATA_KEY, granted)
                    }
                }
            }

            /**
             * Class use to build a sync message.
             * When the service receive a sync message it responses with two messages,
             * the first message contains the service's state and the second message contains the
             * service's configuration
             *
             * @see Communication.V1.Out.ServiceNotification
             * @see Communication.V1.Out.CurrentServiceConfiguration
             *
             */
            class Sync(replyTo: Messenger) : WithReplyTo(replyTo, ID) {
                companion object {
                    const val ID = 8
                }
            }

            /**
             * Class use to build ForcePing message.
             * When the service receive a force ping message it pings the service
             */
            object ForcePing : In(7)
        }


        /**
         * Class that maps all messages that the [com.kynetics.uf.android.UpdateFactoryService]
         * sends to the clients
         *
         * @property id message code so that the recipient can identify what this message is about
         *  ([android.os.Message.what])
         */
        sealed class Out(id: Int) : V1(id) {
            /**
             * This class represents a message that the [com.kynetics.uf.android.UpdateFactoryService]
             * sends to clients with the information about its state. This message is sent after each
             * polling request or as response of a [Communication.V1.In.Sync] message.
             *
             * @property content is the representation of the current service's state
             * @see UFServiceMessageV1
             */
            class ServiceNotification(val content: UFServiceMessageV1) : Out(ID) {
                /**
                 * @suppress
                 */
                companion object {
                    const val ID = 4
                }
            }

            /**
             * This class represents a message that the [com.kynetics.uf.android.UpdateFactoryService]
             * sends to clients when it is waiting for an user authorization
             *
             * @property authName is the kind of authorization, it is one between *DOWNLOAD* and *UPDATE*
             */
            class AuthorizationRequest(val authName: String) : Out(ID) {
                /**
                 * @suppress
                 */
                companion object {
                    const val ID = 5
                }
            }

            /**
             * This class represents a message that the [com.kynetics.uf.android.UpdateFactoryService]
             * sends to the client as response of a [Communication.V1.In.Sync] message.
             * @property conf is the service's configuration
             */
            @Deprecated("As of release 1.3.0 replaced by com.kynetics.uf.android.api.Communication.V1.Out.CurrentServiceConfigurationV2")
            @Suppress("DEPRECATION", "MemberVisibilityCanBePrivate")
            class CurrentServiceConfiguration(val conf: UFServiceConfiguration) : Out(ID) {
                /**
                 * @suppress
                 */
                companion object {
                    const val ID = 9
                }
            }

            /**
             * This class represents a message that the [com.kynetics.uf.android.UpdateFactoryService]
             * sends to the client as response of a [Communication.V1.In.Sync] message.
             * @property conf is the service's configuration
             */
            class CurrentServiceConfigurationV2(@Suppress("MemberVisibilityCanBePrivate")  val conf: UFServiceConfigurationV2) : Out(ID) {
                /**
                 * @suppress
                 */
                companion object {
                    const val ID = 11
                }
            }
        }
    }
}
