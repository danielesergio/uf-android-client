/*
 *
 *  Copyright © 2017-2019  Kynetics  LLC
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.kynetics.uf.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kynetics.uf.android.UpdateFactoryService;
import com.kynetics.uf.android.apicomptibility.ApiVersion;
import com.kynetics.uf.android.update.CurrentUpdateState;

public class StartServiceReceiver extends BroadcastReceiver {
    private static final String TAG = StartServiceReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null){
            return;
        }
        final String action = intent.getAction();
        final boolean ufServiceIsUpdated = Intent.ACTION_MY_PACKAGE_REPLACED.equals(action);
        if(ufServiceIsUpdated){
            Log.d(TAG, "Uf service is updated");
            new CurrentUpdateState(context).setUFUpdated();
        }
        if (ufServiceIsUpdated || Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            final Intent myIntent = new Intent(context, UpdateFactoryService.class);
            ApiVersion.fromVersionCode().startService(context, myIntent);
        }
    }
}
