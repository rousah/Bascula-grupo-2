package com.example.rousah.bascula;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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
        FLAG_LLAMANDO = 1;
        builder = new AlertDialog.Builder(this, R.style.Dialog)
                .setTitle("Llamar a emergencias")
                .setMessage("Se ha detectado una caída. ¿Quiere llamar a emergencias? Se llamará por defecto en 10 segundos si no cancela.")
                .setPositiveButton("Llamar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:123456789"));
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(callIntent);
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FLAG_LLAMANDO = 0;
                                dialog.cancel();
                                finish();
                            }
                        }
                );
        dialog = builder.show();

        final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (FLAG_LLAMANDO == 1) {
                        dialog.dismiss();
                        final Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:123456789"));
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(callIntent);
                        FLAG_LLAMANDO = 0;
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
            }
        }, 20000);


    }
}
