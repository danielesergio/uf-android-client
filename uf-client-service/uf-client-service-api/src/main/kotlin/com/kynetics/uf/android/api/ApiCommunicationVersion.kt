/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.api

/**
 * Enum class that represents all supported api communication versions.
 *
 * @property versionCode the ApiCommunicationVersion's version code number
 * @property versionName the ApiCommunicationVersion's version name
 */
@Suppress("MemberVisibilityCanBePrivate")
enum class ApiCommunicationVersion(
    /**
     * the ApiCommunicationVersion's version code number
     */
    val versionCode: Int,
    /**
     * the ApiCommunicationVersion's version name
     */
    val versionName: String) {

    /**
     * Api version 0.1
     */
    V0_1(0, "0.1"),

    /**
     * Api version 1.0
     */
    V1(1, "1.0"),

    /**
     * Api version 1.1
     */
    V1_1(2, "1.1.0");

    companion object {

        /**
         * Utility method to obtains an [ApiCommunicationVersion] from its version code
         * @return the ApiCommunicationVersion object matching the given [versionCode].
         * @throws [NoSuchElementException] if no such element is found.
         */
        @JvmStatic
        fun fromVersionCode(
            /**
             * ApiCommunicationVersion's version code
             */
            versionCode: Int): ApiCommunicationVersion {
            return values()
                .first {
                    it.versionCode == versionCode
                }
        }
    }
}
