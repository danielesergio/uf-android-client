package com.kynetics.uf.android.configuration

import org.eclipse.hara.ddiclient.api.ConfigDataProvider

interface TargetAttributesHandler{
    fun getConfigurationTargetAttributes(): Map<String, String>
    fun saveConfigurationTargetAttributes(targetAttributes:Map<String, String>)
    fun saveAddTargetAttributes(targetAttributes:Map<String, String>)
    fun newConfigDataProvider(): ConfigDataProvider

}
