package com.example.rousah.bascula;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class ReceptorWifi extends BroadcastReceiver {
    public static NetworkInfo estado;
    private static Intent i;

    @Override public void onReceive(final Context context, Intent intent) {
        // Sacamos información de la intención
        Bundle extras = intent.getExtras();

        if (extras != null) {
            estado = extras.getParcelable("networkInfo");
            if (estado.isConnectedOrConnecting()) {
                Log.d("ReceptorAnuncio", estado + " intent=" + intent);
                if (DialogWifi.dialog != null && DialogWifi.dialog.isShowing()) {
                    DialogWifi.esto.finish();
                    i = null;
                    DialogWifi.dialog = null;
                }
            }
            if (!estado.isConnectedOrConnecting() && i == null) {
                i = new Intent();
                i.setClassName("com.example.rousah.bascula", "com.example.rousah.bascula.DialogWifi");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}