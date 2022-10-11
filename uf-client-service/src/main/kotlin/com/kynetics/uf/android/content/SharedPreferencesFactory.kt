package com.kynetics.uf.android.content

import android.content.Context
import com.kynetics.uf.android.R

object SharedPreferencesFactory {

    fun get(context: Context, name:String?, mode:Int):SharedPreferencesWithObject{
        return UFSharedPreferences(
            SharedPreferencesWithObjectImpl(context.getSharedPreferences(name, mode)),
            EncryptedSharedPreferences.get(context),
            arrayOf(context.getString(R.string.shared_preferences_gateway_token_key),
                context.getString(R.string.shared_preferences_target_token_key),
                context.getString(R.string.shared_preferences_target_token_received_from_server_key)))

    }
}