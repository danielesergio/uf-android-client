package com.kynetics.uf.android.formatter

fun String?.toPwdFormat():String = "*".repeat(this?.length ?: 0)