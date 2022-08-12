package com.kynetics.uf.ddiclient

import okhttp3.OkHttpClient
import org.eclipse.hara.ddi.security.Authentication
import org.eclipse.hara.ddiclient.api.*


object HaraClientFactory {

    private fun newInstance(
        haraClientData: HaraClientData,
        directoryForArtifactsProvider: DirectoryForArtifactsProvider,
        configDataProvider: ConfigDataProvider,
        deploymentPermitProvider: DeploymentPermitProvider,
        messageListeners: List<MessageListener>,
        updaters: List<Updater>,
        forceDeploymentPermitProvider: DeploymentPermitProvider,
        builder:OkHttpClient.Builder
    ): HaraClient  = HaraClientDefaultImpl().apply{
        init(haraClientData,
            directoryForArtifactsProvider,
            configDataProvider,
            deploymentPermitProvider,
            messageListeners,
            updaters,
            DownloadBehaviorImpl(),
            forceDeploymentPermitProvider,
            builder)
    }

    fun newUFClient(
        haraClientData: HaraClientData,
        directoryForArtifactsProvider: DirectoryForArtifactsProvider,
        configDataProvider: ConfigDataProvider,
        deploymentPermitProvider: DeploymentPermitProvider,
        messageListeners: List<MessageListener>,
        updaters: List<Updater>,
        forceDeploymentPermitProvider: DeploymentPermitProvider,
        targetTokenFoundListener:TargetTokenFoundListener):HaraClient =
        newInstance(haraClientData,
            directoryForArtifactsProvider,
            configDataProvider,
            deploymentPermitProvider,
            messageListeners,
            updaters,
            forceDeploymentPermitProvider,
            getOkHttpBuilder(targetTokenFoundListener, haraClientData))

    fun newHawkbitClient(
        haraClientData: HaraClientData,
        directoryForArtifactsProvider: DirectoryForArtifactsProvider,
        configDataProvider: ConfigDataProvider,
        deploymentPermitProvider: DeploymentPermitProvider,
        messageListeners: List<MessageListener>,
        forceDeploymentPermitProvider: DeploymentPermitProvider,
        updaters: List<Updater>):HaraClient =
        newInstance(haraClientData,
            directoryForArtifactsProvider,
            configDataProvider,
            deploymentPermitProvider,
            messageListeners,
            updaters,
            forceDeploymentPermitProvider,
            OkHttpClient.Builder())


    private fun getOkHttpBuilder(listener:TargetTokenFoundListener,clientData:HaraClientData): OkHttpClient.Builder{
        val authentications = HashSet<Authentication>()
        with(clientData) {
            if (gatewayToken != null) {
                authentications.add(Authentication.newInstance(Authentication.AuthenticationType.GATEWAY_TOKEN_AUTHENTICATION, gatewayToken!!))
            }
            if (targetToken != null) {
                authentications.add(Authentication.newInstance(Authentication.AuthenticationType.TARGET_TOKEN_AUTHENTICATION, targetToken!!))
            }
        }
        val authentication=UpdateFactoryAuthenticationRequestInterceptor(authentications,listener)
        val builder=OkHttpClient.Builder()
        builder.interceptors().add(authentication)
        return builder
    }

}
