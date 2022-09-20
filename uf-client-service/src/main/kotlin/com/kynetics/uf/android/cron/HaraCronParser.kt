package com.kynetics.uf.android.cron

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser

object HaraCronParser: CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))