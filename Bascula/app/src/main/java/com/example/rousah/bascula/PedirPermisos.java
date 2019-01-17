package com.example.rousah.bascula;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

public class PedirPermisos {

    public static void solicitarPermiso(final String[] permiso, String
            justificacion, final int requestCode, final Activity actividad) {
        ActivityCompat.requestPermissions(actividad, permiso, requestCode);
    }
}
