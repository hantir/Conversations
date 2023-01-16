package com.frizid.timeline.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.installations.FirebaseInstallations;

import com.frizid.timeline.Config;
import com.frizid.timeline.utils.Compatibility;

public class MaintenanceReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Config.LOGTAG, "received intent in maintenance receiver");
		if ("com.frizid.timeline.RENEW_INSTANCE_ID".equals(intent.getAction())) {
			renewInstanceToken(context);

		}
	}

	private void renewInstanceToken(final Context context) {
		FirebaseInstallations.getInstance().delete().addOnSuccessListener(unused -> {
			final Intent intent = new Intent(context, XmppConnectionService.class);
				intent.setAction(XmppConnectionService.ACTION_FCM_TOKEN_REFRESH);
				Compatibility.startService(context, intent);
		});
	}
}
