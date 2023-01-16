package com.frizid.timeline.services;

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.firebase.messaging.FirebaseMessaging;

import com.frizid.timeline.Config;
import com.frizid.timeline.R;
import com.frizid.timeline.entities.Account;
import com.frizid.timeline.utils.PhoneHelper;
import com.frizid.timeline.xml.Element;
import com.frizid.timeline.xml.Namespace;
import com.frizid.timeline.xmpp.Jid;
import com.frizid.timeline.xmpp.XmppConnection;
import com.frizid.timeline.xmpp.forms.Data;
import com.frizid.timeline.xmpp.stanzas.IqPacket;

public class PushManagementService {

    protected final XmppConnectionService mXmppConnectionService;

    PushManagementService(XmppConnectionService service) {
        this.mXmppConnectionService = service;
    }

    private static Data findResponseData(IqPacket response) {
        final Element command = response.findChild("command", Namespace.COMMANDS);
        final Element x = command == null ? null : command.findChild("x", Namespace.DATA);
        return x == null ? null : Data.parse(x);
    }

    private Jid getAppServer() {
        return Jid.of(mXmppConnectionService.getString(R.string.app_server));
    }

    void registerPushTokenOnServer(final Account account) {
        Log.d(Config.LOGTAG, account.getJid().asBareJid() + ": has push support");
        retrieveFcmInstanceToken(token -> {
            final String androidId = PhoneHelper.getAndroidId(mXmppConnectionService);
            final IqPacket packet = mXmppConnectionService.getIqGenerator().pushTokenToAppServer(getAppServer(), token, androidId);
            mXmppConnectionService.sendIqPacket(account, packet, (a, response) -> {
                final Data data = findResponseData(response);
                if (response.getType() == IqPacket.TYPE.RESULT && data != null) {
                    try {
                        String node = data.getValue("node");
                        String secret = data.getValue("secret");
                        Jid jid = Jid.of(data.getValue("jid"));
                        if (node != null && secret != null) {
                            enablePushOnServer(a, jid, node, secret);
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(Config.LOGTAG, a.getJid().asBareJid() + ": failed to enable push. invalid response from app server " + response);
                }
            });
        });
    }

    private void enablePushOnServer(final Account account, final Jid appServer, final String node, final String secret) {
        final IqPacket enable = mXmppConnectionService.getIqGenerator().enablePush(appServer, node, secret);
        mXmppConnectionService.sendIqPacket(account, enable, (a, p) -> {
            if (p.getType() == IqPacket.TYPE.RESULT) {
                Log.d(Config.LOGTAG, a.getJid().asBareJid() + ": successfully enabled push on server");
            } else if (p.getType() == IqPacket.TYPE.ERROR) {
                Log.d(Config.LOGTAG, a.getJid().asBareJid() + ": enabling push on server failed");
            }
        });
    }

    private void retrieveFcmInstanceToken(final OnGcmInstanceTokenRetrieved instanceTokenRetrieved) {
        final FirebaseMessaging firebaseMessaging;
        try {
            firebaseMessaging = FirebaseMessaging.getInstance();
        } catch (IllegalStateException e) {
            Log.d(Config.LOGTAG, "unable to get firebase instance token ", e);
            return;
        }
        firebaseMessaging.getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d(Config.LOGTAG, "unable to get Firebase instance token", task.getException());
            }
            final String result;
            try {
                result = task.getResult();
            } catch (Exception e) {
                Log.d(Config.LOGTAG, "unable to get Firebase instance token due to bug in library ", e);
                return;
            }
            if (result != null) {
                instanceTokenRetrieved.onGcmInstanceTokenRetrieved(result);
            }
        });

    }


    public boolean available(Account account) {
        final XmppConnection connection = account.getXmppConnection();
        return connection != null
                && connection.getFeatures().sm()
                && connection.getFeatures().push()
                && playServicesAvailable();
    }

    private boolean playServicesAvailable() {
        return GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(mXmppConnectionService) == ConnectionResult.SUCCESS;
    }

    public boolean isStub() {
        return false;
    }

    interface OnGcmInstanceTokenRetrieved {
        void onGcmInstanceTokenRetrieved(String token);
    }
}
