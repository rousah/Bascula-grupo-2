package com.example.rousah.bascula;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class DialogWifi extends Activity {
    public static AlertDialog.Builder builder;
    public static AlertDialog dialog;
    public static Activity esto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        esto = this;

        if (dialog == null) {
            builder = new AlertDialog.Builder(this, R.style.Dialog)
                    .setTitle("Desconexión de wifi")
                    .setMessage("No tiene conexión a internet. Para poder usar la aplicación, conéctese.")
                    .setIcon(R.drawable.ic_round_wifi_off_24px)
                    .setCancelable(false);
            dialog = builder.show();
        }
    }
}
