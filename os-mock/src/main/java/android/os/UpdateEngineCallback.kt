/*
 * Copyright © 2017-2022  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package android.os

abstract class UpdateEngineCallback {
    /**
     * Invoked when anything changes. The value of `status` will
     * be one of the values from [UpdateEngine.UpdateStatusConstants],
     * and `percent` will be valid [TODO: in which cases?].
     */
    abstract fun onStatusUpdate(status: Int, percent: Float)

    /**
     * Invoked when the payload has been applied, whether successfully or
     * unsuccessfully. The value of `errorCode` will be one of the
     * values from [UpdateEngine.ErrorCodeConstants].
     */
    abstract fun onPayloadApplicationComplete(error: Int)
}