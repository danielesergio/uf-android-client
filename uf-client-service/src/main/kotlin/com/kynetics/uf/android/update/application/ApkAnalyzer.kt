/*
 * Copyright © 2017-2020  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.update.application

import android.content.Context
import android.content.pm.PackageManager

object ApkAnalyzer {

    fun getPackageFromApk(context: Context, apkPath: String): String? {
        val packageInfo = context.packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES)
        if (packageInfo != null) {
            val appInfo = packageInfo.applicationInfo
            return appInfo.packageName
        }
        return null
    }

    @Suppress("DEPRECATION")
    fun getVersionFromApk(context: Context, apkPath: String): Long? {
        val packageInfo = context.packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES)
        if (packageInfo != null) {
            return packageInfo.versionCode.toLong()
        }
        return null
    }

    fun verifySharedUserId(context: Context, apkPath: String): Boolean {
        val packageInfo = context.packageManager
            .getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES)
        val apkPackage = packageInfo?.packageName
        if (packageInfo != null && apkPackage != null) {
            val newSharedUserId = packageInfo.sharedUserId
            return try {
                val oldSharedUserId = context.packageManager
                    .getPackageInfo(apkPackage, 0).sharedUserId
                newSharedUserId == oldSharedUserId
            } catch (e: PackageManager.NameNotFoundException) {
                true
            }
        }
        return false
    }
}
