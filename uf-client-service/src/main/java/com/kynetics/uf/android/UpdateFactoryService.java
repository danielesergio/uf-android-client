/*
 * Copyright © 2017-2018  Kynetics  LLC
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.kynetics.uf.android.api.UFServiceConfiguration;
import com.kynetics.uf.android.api.UFServiceMessage;
import com.kynetics.uf.android.api.UFServiceMessage.Suspend;
import com.kynetics.uf.android.content.SharedPreferencesWithObject;
import com.kynetics.uf.android.ui.MainActivity;
import com.kynetics.updatefactory.ddiclient.api.ClientBuilder;
import com.kynetics.updatefactory.ddiclient.api.api.DdiRestApi;
import com.kynetics.updatefactory.ddiclient.core.UFService;
import com.kynetics.updatefactory.ddiclient.core.model.Event;
import com.kynetics.updatefactory.ddiclient.core.model.State;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import okhttp3.OkHttpClient;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.MSG_AUTHORIZATION_REQUEST;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.MSG_AUTHORIZATION_RESPONSE;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.MSG_CONFIGURE_SERVICE;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.MSG_REGISTER_CLIENT;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.MSG_RESUME_SUSPEND_UPGRADE;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.MSG_SEND_STRING;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.MSG_SERVICE_CONFIGURATION_STATUS;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.MSG_SYNCH_REQUEST;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.MSG_UNREGISTER_CLIENT;
import static com.kynetics.uf.android.api.UFServiceCommunicationConstants.SERVICE_DATA_KEY;
import static com.kynetics.uf.android.api.UFServiceMessage.Suspend.DOWNLOAD;
import static com.kynetics.uf.android.api.UFServiceMessage.Suspend.NONE;
import static com.kynetics.uf.android.api.UFServiceMessage.Suspend.UPDATE;

/**
 * @author Daniele Sergio
 */
public class UpdateFactoryService extends Service implements UpdateFactoryServiceCommand {
    private static final String TAG = UpdateFactoryService.class.getSimpleName();

    public static UpdateFactoryServiceCommand getUFServiceCommand(){
        return ufServiceCommand;
    }

    @Override
    public void authorizationGranted() {
        ufService.setAuthorized(true);
    }

    @Override
    public void authorizationDenied() {
        ufService.setAuthorized(false);
    }


    @Override
    public void configureService() {
        if(ufService!=null){
            ufService.stop();
        }
        buildServiceFromPreferences(true); // TODO: 11/14/17 fix workaround
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSharedPreferencesKeys();
        ufServiceCommand = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Serializable serializable = intent.getSerializableExtra(SERVICE_DATA_KEY);
        boolean startNewService = false;
        if(serializable instanceof UFServiceConfiguration){
            final UFServiceConfiguration serviceConfiguration = (UFServiceConfiguration) serializable;
            saveServiceConfigurationToSharedPreferences(serviceConfiguration);
            startNewService = true;
        }
        buildServiceFromPreferences(startNewService);
        return START_STICKY;
    }

    private OkHttpClient.Builder buildOkHttpClient() {
        return new OkHttpClient.Builder();
    }

    private State getInitialState(boolean startNewService,
                                  SharedPreferencesWithObject sharedPreferences) {
        final Long updatePendingId = UpdateSystem.getUpdatePendingId();
        if(updatePendingId != null){
            Log.e(TAG, "updatePendingId found: " + updatePendingId);
            return new State.UpdateStartedState(updatePendingId);
        }
        return startNewService ? new State.WaitingState(0, null) : sharedPreferences.getObject(sharedPreferencesCurrentStateKey, State.class, new State.WaitingState(0, null));
    }

    private void buildServiceFromPreferences(boolean startNewService) {
        startStopService(false);
        final SharedPreferencesWithObject sharedPreferences = getSharedPreferences(sharedPreferencesFile, MODE_PRIVATE);
        final boolean serviceIsEnable = sharedPreferences.getBoolean(sharedPreferencesServiceEnableKey, false);
        if(serviceIsEnable) {
            final String url = sharedPreferences.getString(sharedPreferencesServerUrlKey, "");
            final String controllerId = sharedPreferences.getString(sharedPreferencesControllerIdKey, "");
            final String gatewayToken = sharedPreferences.getString(sharedPreferencesGatewayToken, "");
            final String targetToken = sharedPreferences.getString(sharedPreferencesTargetToken, "");
            final String tenant = sharedPreferences.getString(sharedPreferencesTenantKey, "");
            final long delay = sharedPreferences.getLong(sharedPreferencesRetryDelayKey, 30000);
            final State initialState = getInitialState(startNewService, sharedPreferences);
            final boolean apiMode = sharedPreferences.getBoolean(sharedPreferencesApiModeKey, true);
            final HashMap<String,String> defaultArgs = new HashMap<>();
            final Map<String,String> args = sharedPreferences.getObject(sharedPreferencesArgs, defaultArgs.getClass());
            try {
                final DdiRestApi client = new ClientBuilder()
                        .withBaseUrl(url)
                        .withGatewayToken(gatewayToken)
                        .withTargetToken(targetToken)
                        .withHttpBuilder(buildOkHttpClient())
                        .build();
                ufService = UFService.builder()
                        .withClient(client)
                        .withRetryDelayOnCommunicationError(delay)
                        .withTenant(tenant)
                        .withControllerId(controllerId)
                        .withInitialState(initialState)
                        .withTargetData(()->args)
                        .build();

                ufService.addObserver(new ObserverState(apiMode));
                startStopService(true);
                if (initialState.getStateName() == State.StateName.UPDATE_STARTED) {
                    ufService.setUpdateSucceffullyUpdate(UpdateSystem.successInstallation());
                }
            }catch (IllegalStateException | IllegalArgumentException e){
                sharedPreferences.edit().putBoolean(sharedPreferencesServiceEnableKey, false).apply();
                Toast.makeText(this,"Update Factory configuration error",Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    private UFServiceConfiguration getCurrentConfiguration(SharedPreferencesWithObject sharedPreferences){
        final boolean serviceIsEnable = ufService != null && sharedPreferences.getBoolean(sharedPreferencesServiceEnableKey, false);
        final String url = sharedPreferences.getString(sharedPreferencesServerUrlKey, "");
        final String controllerId = sharedPreferences.getString(sharedPreferencesControllerIdKey, "");
        final String gatewayToken = sharedPreferences.getString(sharedPreferencesGatewayToken, "");
        final String targetToken = sharedPreferences.getString(sharedPreferencesTargetToken, "");
        final String tenant = sharedPreferences.getString(sharedPreferencesTenantKey, "");
        final long delay = sharedPreferences.getLong(sharedPreferencesRetryDelayKey, 30000);
        final boolean apiMode = sharedPreferences.getBoolean(sharedPreferencesApiModeKey, true);
        final Map<String,String> args = sharedPreferences.getObject(sharedPreferencesArgs, new HashMap<String,String>().getClass());
        return UFServiceConfiguration.builder()
                .witArgs(args)
                .witEnable(serviceIsEnable)
                .withApiMode(apiMode)
                .withControllerId(controllerId)
                .withGetawayToken(gatewayToken)
                .withRetryDelay(delay)
                .withTargetToken(targetToken)
                .withTenant(tenant)
                .withUrl(url)
                .build();
    }
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONFIGURE_SERVICE:
                    final UFServiceConfiguration configuration = (UFServiceConfiguration) msg.getData().getSerializable(SERVICE_DATA_KEY);
                    saveServiceConfigurationToSharedPreferences(configuration);
                    buildServiceFromPreferences(true);
                    break;
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_AUTHORIZATION_RESPONSE:
                    ufService.setAuthorized(msg.getData().getBoolean(SERVICE_DATA_KEY));
                    break;
                case MSG_RESUME_SUSPEND_UPGRADE:
                    ufService.restartSuspendState();
                    break;
                case MSG_SYNCH_REQUEST:
                    final SharedPreferencesWithObject sharedPreferences = getSharedPreferences(sharedPreferencesFile, MODE_PRIVATE);
                    UpdateFactoryService.this.sendMessage(getCurrentConfiguration(sharedPreferences), MSG_SERVICE_CONFIGURATION_STATUS, msg.replyTo);
                    if(ufService == null){
                        return;
                    }
                    final UFServiceMessage lastMessage = sharedPreferences.getObject(SHARED_PREFERENCES_LAST_NOTIFY_MESSAGE, UFServiceMessage.class);
                    if(lastMessage != null){
                        UpdateFactoryService.this.sendMessage(lastMessage, MSG_SEND_STRING, msg.replyTo);
                    }
                    State lastState = sharedPreferences.getObject(sharedPreferencesCurrentStateKey, State.class);
                    if(lastState.getStateName() == State.StateName.AUTHORIZATION_WAITING){
                        UpdateFactoryService.this.sendMessage(((State.AuthorizationWaitingState)lastState).getState().getStateName().name(),
                                MSG_AUTHORIZATION_REQUEST,
                                msg.replyTo);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void saveServiceConfigurationToSharedPreferences(UFServiceConfiguration configuration) {
        SharedPreferencesWithObject sharedPreferences;
        sharedPreferences = getSharedPreferences(sharedPreferencesFile, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(sharedPreferencesControllerIdKey, configuration.getControllerId());
        editor.putString(sharedPreferencesTenantKey, configuration.getTenant());
        editor.putString(sharedPreferencesServerUrlKey, configuration.getUrl());
        editor.putString(sharedPreferencesGatewayToken, configuration.getGatewayToken());
        editor.putString(sharedPreferencesTargetToken, configuration.getTargetToken());
        editor.putLong(sharedPreferencesRetryDelayKey, configuration.getRetryDelay());
        editor.putBoolean(sharedPreferencesApiModeKey, configuration.isApiMode());
        editor.putBoolean(sharedPreferencesServiceEnableKey, configuration.isEnalbe());
        editor.apply();
        sharedPreferences.putAndCommitObject(sharedPreferencesArgs,configuration.getArgs());
    }

    private void startStopService(boolean serviceIsEnable) {
        if(ufService == null){
            return;
        }
        if(serviceIsEnable) {
            ufService.start();
        } else {
            ufService.stop();
        }
    }

    private void sendMessage(Serializable messageContent, int code, Messenger messenger) {
        final Message message = getMessage(messageContent, code);
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Serializable messageContent, int code){
        final Message message = getMessage(messageContent, code);
        int i = 0;
        while (i<mClients.size()) {
            try {
                mClients.get(i).send(message);
                i++;
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    @NonNull
    private Message getMessage(Serializable messageContent, int code) {
        final Message message = Message.obtain(null, code);
        final Bundle data = new Bundle();
        data.putSerializable(SERVICE_DATA_KEY, messageContent);
        message.setData(data);
        return message;
    }

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private class ObserverState implements Observer {
        private final boolean apiMode;

        public ObserverState(boolean apiMode) {
            this.apiMode = apiMode;
        }

        @Override
        public void update(Observable observable, Object o) {
            if(o instanceof UFService.SharedEvent) {
                final UFService.SharedEvent eventNotify = (UFService.SharedEvent) o;
                final Event event = eventNotify.getEvent();
                final State newState = eventNotify.getNewState();

                final UFServiceMessage message = new UFServiceMessage(
                        event.getEventName().name(),
                        eventNotify.getOldState().getStateName().name(),
                        newState.getStateName().name(),
                        getSuspend(newState)
                );
                writeObjectToSharedPreference(message, SHARED_PREFERENCES_LAST_NOTIFY_MESSAGE);
                sendMessage(message, MSG_SEND_STRING);
                writeObjectToSharedPreference(eventNotify.getNewState(), sharedPreferencesCurrentStateKey);
                switch (newState.getStateName()){
                    case AUTHORIZATION_WAITING:
                        final State.StateName auth = ((State.AuthorizationWaitingState) newState).getState().getStateName();
                        if(apiMode){
                            sendMessage(auth.name(), MSG_AUTHORIZATION_REQUEST);
                        }else {
                            showAuthorizationDialog(auth);
                        }
                        break;
                    case SAVING_FILE:
                        final State.SavingFileState savingFileState = ((State.SavingFileState) newState);
                        UpdateSystem.copyFile(savingFileState.getInputStream());
                        break;
                    case UPDATE_STARTED:
                        if(UpdateSystem.verify()){
                            UpdateSystem.install(getApplicationContext(), ((State.StateWithAction)newState).getActionId());
                        } else {
                            ufService.setUpdateSucceffullyUpdate(false);
                            Toast.makeText(getApplicationContext(),getString(R.string.invalid_update), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        }
    }

    private void showAuthorizationDialog(State.StateName auth) {
        final Intent intent = new Intent(UpdateFactoryService.this, MainActivity.class);
        intent.putExtra(MainActivity.INTENT_TYPE_EXTRA_VARIABLE, auth == State.StateName.UPDATE_DOWNLOAD ?
                MainActivity.INTENT_TYPE_EXTRA_VALUE_DOWNLOAD : MainActivity.INTENT_TYPE_EXTRA_VALUE_REBOOT);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private Suspend getSuspend(State state){
        if(state.getStateName() != State.StateName.WAITING){
            return NONE;
        }
        State.WaitingState waitingState = (State.WaitingState) state;
        if(!waitingState.hasInnerState()){
            return NONE;
        }
        return waitingState.getState().getStateName() == State.StateName.UPDATE_DOWNLOAD ?
                DOWNLOAD : UPDATE;
    }

    @Override
    public SharedPreferencesWithObject getSharedPreferences(String name, int mode){
        return new SharedPreferencesWithObject(super.getSharedPreferences(name, mode));
    }

    private void writeObjectToSharedPreference(Serializable obj, String key){
        final SharedPreferencesWithObject sharedPreferences = getSharedPreferences(sharedPreferencesFile,MODE_PRIVATE);
        sharedPreferences.putAndCommitObject(key, obj);
    }

    private void initSharedPreferencesKeys(){
        sharedPreferencesFile = getString(R.string.shared_preferences_file);
        sharedPreferencesCurrentStateKey = getString(R.string.shared_preferences_current_state_key);
        sharedPreferencesServerUrlKey = getString(R.string.shared_preferences_server_url_key);
        sharedPreferencesApiModeKey = getString(R.string.shared_preferences_api_mode_key);
        sharedPreferencesTenantKey = getString(R.string.shared_preferences_tenant_key);
        sharedPreferencesControllerIdKey = getString(R.string.shared_preferences_controller_id_key);
        sharedPreferencesRetryDelayKey = getString(R.string.shared_preferences_retry_delay_key);
        sharedPreferencesServiceEnableKey = getString(R.string.shared_preferences_is_enable_key);
        sharedPreferencesGatewayToken = getString(R.string.shared_preferences_gateway_token_key);
        sharedPreferencesTargetToken = getString(R.string.shared_preferences_target_token_key);
        sharedPreferencesArgs = getString(R.string.shared_preferences_args_key);
    }

    private static UpdateFactoryServiceCommand ufServiceCommand;
    private UFService ufService;
    private String sharedPreferencesCurrentStateKey;
    private String sharedPreferencesServerUrlKey;
    private String sharedPreferencesApiModeKey;
    private String sharedPreferencesServiceEnableKey;
    private String sharedPreferencesTenantKey;
    private String sharedPreferencesControllerIdKey;
    private String sharedPreferencesRetryDelayKey;
    private String sharedPreferencesGatewayToken;
    private String sharedPreferencesTargetToken;
    private String sharedPreferencesFile;
    private String sharedPreferencesArgs;

    private static final String SHARED_PREFERENCES_LAST_NOTIFY_MESSAGE = "LAST_NOTIFY_MESSAGE";

}