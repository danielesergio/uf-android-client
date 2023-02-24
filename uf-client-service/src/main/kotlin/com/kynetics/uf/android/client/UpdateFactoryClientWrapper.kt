/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.client

import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import org.eclipse.hara.ddiclient.api.*

class UpdateFactoryClientWrapper(@Volatile var delegate: HaraClient? = null): HaraClient {
    override fun forcePing() {
        delegate?.forcePing()
    }

    override fun init(haraClientData: HaraClientData,
                      directoryForArtifactsProvider: DirectoryForArtifactsProvider,
                      configDataProvider: ConfigDataProvider,
                      softDeploymentPermitProvider: DeploymentPermitProvider,
                      messageListeners: List<MessageListener>,
                      updaters: List<Updater>,
                      downloadBehavior: DownloadBehavior,
                      forceDeploymentPermitProvider: DeploymentPermitProvider,
                      httpBuilder: OkHttpClient.Builder,
                      scope:CoroutineScope) {
        delegate?.init(haraClientData,
            directoryForArtifactsProvider,
            configDataProvider,
            softDeploymentPermitProvider,
            messageListeners,
            updaters.toList(),
            downloadBehavior,
            forceDeploymentPermitProvider,
            httpBuilder,
            scope)
    }

    override fun startAsync() {
        delegate?.startAsync()
    }

    override fun stop() {
        delegate?.stop()
    }
}