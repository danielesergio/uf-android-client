package com.kynetics.uf.android.configuration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kynetics.uf.android.api.UFServiceConfigurationV2
import com.kynetics.uf.android.content.SharedPreferencesWithObjectImpl
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class TargetAttributesHandlerImplTest {

    companion object{
        const val UF_KEY_1 = "uf_key_1"
        const val UF_KEY_2 = "UF_key_2"
        const val UF_KEY_DEFINED_BY_UF_ANDROID_CLIENT = "client_version"
        const val THIRD_PARTY_KEY = "third_party_key"
        private const val CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        fun targetAttributeValueRandomValue():String = (1..12)
            .map { CHAR_SET.random() }
            .joinToString("")
    }

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val keys = SharedPreferencesKeys.getInstance(context)
    private val sp = SharedPreferencesWithObjectImpl(context.getSharedPreferences("file1",Context.MODE_PRIVATE))

    private val targetAttributesHandler = TargetAttributesHandlerImpl(sp, keys, UFServiceConfigurationV2.TimeWindows())
    private val configDataProvider = targetAttributesHandler.newConfigDataProvider()
    @Test
    fun testTargetAttributesDoesNotContainsKeyStartingWithUFDefinedByThirdPartyApp() {
        targetAttributesHandler.saveAddTargetAttributes(mapOf(
            UF_KEY_1 to targetAttributeValueRandomValue(),
            UF_KEY_2 to targetAttributeValueRandomValue())
        )

        val targetAttributes = configDataProvider.configData()

        Assert.assertFalse(
            "targetAttributes should not contains keys: [$UF_KEY_1, $UF_KEY_2]",
            targetAttributes.containsKey(UF_KEY_1) && targetAttributes.containsKey(UF_KEY_2)
        )
    }

    @Test
    fun testTargetAttributesDefinedByUFAndroidClientAreNotOverwrittenByThirdPartyApp() {
        val newTAValue = targetAttributeValueRandomValue()
        val originalTAValue = configDataProvider.configData()[UF_KEY_DEFINED_BY_UF_ANDROID_CLIENT]

        targetAttributesHandler.saveAddTargetAttributes(mapOf(
            UF_KEY_DEFINED_BY_UF_ANDROID_CLIENT to newTAValue)
        )

        val targetAttributes = configDataProvider.configData()

        Assert.assertEquals(
            "$UF_KEY_DEFINED_BY_UF_ANDROID_CLIENT targetAttribute should be $originalTAValue",
            originalTAValue,
            targetAttributes[UF_KEY_DEFINED_BY_UF_ANDROID_CLIENT]
        )
    }

    @Test
    fun testTargetAttributesDefinedByThirdPartyAppAreSuccessfullyStored() {
        val targetAttributeValue = targetAttributeValueRandomValue()

        targetAttributesHandler.saveAddTargetAttributes(mapOf(
            THIRD_PARTY_KEY to targetAttributeValue)
        )

        val targetAttributes = configDataProvider.configData()

        Assert.assertEquals(
            "$THIRD_PARTY_KEY targetAttribute should be $targetAttributeValue",
            targetAttributeValue,
            targetAttributes[THIRD_PARTY_KEY]
        )
    }



}