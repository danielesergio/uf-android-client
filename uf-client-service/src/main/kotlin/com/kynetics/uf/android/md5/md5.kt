package com.kynetics.uf.android.md5

import java.security.MessageDigest

fun String.toMD5(): String {
    return MessageDigest.getInstance("MD5")
        .digest(toByteArray())
        .joinToString("") {
            "%02x".format(it)
        }
}