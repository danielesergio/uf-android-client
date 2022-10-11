/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

@file:Suppress("DEPRECATION")

package com.kynetics.uf.android.configuration

import com.kynetics.uf.android.api.UFServiceConfigurationV2

fun com.kynetics.uf.android.api.UFServiceConfiguration.toUFServiceConfiguration(): UFServiceConfigurationV2{
    @Suppress("USELESS_ELVIS")
    return     UFServiceConfigurationV2(
        tenant = tenant,
        controllerId = controllerId,
        url = url,
        targetToken = targetToken,
        gatewayToken = gatewayToken,
        isApiMode = isApiMode(),
        isEnable = isEnable(),
        isUpdateFactoryServe = isUpdateFactoryServe,
        // targetAttributes could be null when the UFServiceConfiguration is provided from ConfigureService message
        targetAttributes = targetAttributes ?: emptyMap()
    )
}

fun UFServiceConfigurationV2.toUFServiceConfiguration(): com.kynetics.uf.android.api.UFServiceConfiguration =
    com.kynetics.uf.android.api.UFServiceConfiguration.builder()
        .withTenant(tenant)
        .withControllerId(controllerId)
        .withUrl(url)
        .withTargetToken(targetToken)
        .withGatewayToken(gatewayToken)
        .withEnable(isEnable)
        .withApiMode(isApiMode)
        .withIsUpdateFactoryServer(isUpdateFactoryServe)
        .withTargetAttributes(targetAttributes)
        .build()