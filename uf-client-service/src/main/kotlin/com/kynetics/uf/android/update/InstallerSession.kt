/*
 * Copyright © 2017-2020  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.kynetics.uf.android.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kynetics.updatefactory.ddiclient.core.api.Updater
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.concurrent.CountDownLatch

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class InstallerSession private constructor(private val context: Context,
                                           private val packageInstaller: PackageInstaller,
        // TODO: 07/02/19 unregister sessionCallback
                                           private val sessionId: Int) {
    fun writeSession(file: File, name: String?) {
        val sizeBytes = file.length()
        Log.v(TAG, "apk size :$sizeBytes")
        try {
            packageInstaller.openSession(sessionId).use { session ->
                FileInputStream(file).use { `in` ->
                    session.openWrite(name, 0, sizeBytes).use { out ->
                        val buffer = ByteArray(65536)
                        var c: Int
                        while (`in`.read(buffer).also { c = it } != -1) {
                            out.write(buffer, 0, c)
                        }
                        session.fsync(out)
                    }
                }
            }
        } catch (e: IOException) {
            Log.d(TAG, e.message, e)
        }
    }

    fun commitSession() {
        try {
            packageInstaller.openSession(sessionId).use { session -> session.commit(createIntentSender()) }
        } catch (e: IOException) {
            Log.d(TAG, e.message, e)
        }
    }

    private fun createIntentSender(): IntentSender {
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                Intent(ACTION_INSTALL_COMPLETE),
                PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent.intentSender
    }

    companion object {
        const val ACTION_INSTALL_COMPLETE = "com.kynetics.action.INSTALL_COMPLETED"

        @Throws(IOException::class)
        fun newInstance(context: Context,
                        countDownLatch: CountDownLatch?,
                        packageName: String?,
                        packageVersion: Long?,
                        artifact: Updater.SwModuleWithPath.Artifact?,
                        messenger: Updater.Messenger?,
                        currentUpdateState: CurrentUpdateState?): InstallerSession {
            val packageInstaller = context.packageManager
                    .packageInstaller
            val params = SessionParams(
                    SessionParams.MODE_FULL_INSTALL)
            params.setAppPackageName(packageName)
            params.setGrantedRuntimePermissions(null)
            val sessionId = packageInstaller.createSession(params)
            context.registerReceiver(PackageInstallerBroadcastReceiver(
                    sessionId,
                    countDownLatch!!,
                    artifact!!,
                    currentUpdateState!!,
                    messenger!!,
                    packageName!!,
                    packageVersion),
                    IntentFilter(ACTION_INSTALL_COMPLETE))
            return InstallerSession(context, packageInstaller, sessionId)
        }

        private val TAG = InstallerSession::class.java.simpleName
    }

}