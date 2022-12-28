/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.communication

import com.kynetics.uf.android.api.TargetAttributesWithPolicy

@Suppress("ClassName")
interface CommunicationApiV1_2:CommunicationApiV1_1{
    fun addTargetAttributes(targetAttributesWithPolicy: TargetAttributesWithPolicy)
}