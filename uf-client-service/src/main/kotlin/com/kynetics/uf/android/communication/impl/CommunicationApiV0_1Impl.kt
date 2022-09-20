/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.communication.impl

import com.kynetics.uf.android.api.ApiCommunicationVersion
import com.kynetics.uf.android.client.RestartableClientService
import com.kynetics.uf.android.communication.CommunicationApiV0_1
import com.kynetics.uf.android.configuration.AndroidDeploymentPermitProvider
import com.kynetics.uf.android.configuration.ConfigurationHandler

class CommunicationApiV0_1Impl(configurationHandler: ConfigurationHandler,
                               ufService: RestartableClientService,
                               softDeploymentPermitProvider: AndroidDeploymentPermitProvider
):
    AbstractCommunicationApi(configurationHandler, ufService, softDeploymentPermitProvider),
    CommunicationApiV0_1 {
    override val api: ApiCommunicationVersion = ApiCommunicationVersion.V0_1
}