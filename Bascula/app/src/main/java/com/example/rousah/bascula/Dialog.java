package com.example.rousah.bascula;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class Dialog extends Activity {
    public int FLAG_LLAMANDO = 0;
    public static AlertDialog.Builder builder;
    public static AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaPlayer mMediaPlayer;
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
        FLAG_LLAMANDO = 1;
        builder = new AlertDialog.Builder(this, R.style.Dialog)
                .setTitle("Llamar a emergencias")
                .setMessage("Se ha detectado una caída. ¿Quiere llamar a emergencias? Se llamará por defecto en 10 segundos si no cancela.")
                .setPositiveButton("Llamar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + TabPrimero.telefonoEmergencia));
                    //    Log.i("telefono", "tel:" + TabPrimero.telefonoEmergencia);
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(callIntent);
                        r.stop();
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FLAG_LLAMANDO = 0;
                                r.stop();
                                dialog.cancel();
                                finish();
                            }
                        }
                )
                .setCancelable(false)
                .setIcon(R.drawable.ic_danger);

        dialog = builder.show();

        if (!dialog.isShowing()) {
            r.stop();
            finish();
        }

        final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (FLAG_LLAMANDO == 1) {
                        dialog.dismiss();
                        final Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + TabPrimero.telefonoEmergencia));
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(callIntent);
                        FLAG_LLAMANDO = 0;
                        r.stop();
                        finish();
                    }
                }
            }, 10000);

        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent myService = new Intent(getBaseContext(), ServicioCaidas.class);
                startService(myService);
                Log.d("Servicio", "servicio resume");
                finish();
            }
        }, 20000);


    }
}
