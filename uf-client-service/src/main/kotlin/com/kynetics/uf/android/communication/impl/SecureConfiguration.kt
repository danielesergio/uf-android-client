package com.kynetics.uf.android.communication.impl

import com.kynetics.uf.android.api.UFServiceConfigurationV2
import com.kynetics.uf.android.configuration.ConfigurationHandler
import com.kynetics.uf.android.formatter.toPwdFormat

fun ConfigurationHandler.getSecureConfiguration():UFServiceConfigurationV2 =
    getCurrentConfiguration()
        .run{
            copy(
                targetToken = targetToken.toPwdFormat(),
                gatewayToken = gatewayToken.toPwdFormat()
            )
        }
