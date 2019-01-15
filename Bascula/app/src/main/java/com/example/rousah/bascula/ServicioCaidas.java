package com.example.rousah.bascula;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.List;


public class ServicioCaidas extends Service {
    private List<Sensor> listaSensores;
    SensorEventListener listen;
    public static ServicioCaidas servicioCaidas;
    SensorManager sm;

    public ServicioCaidas(){
        servicioCaidas = this;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Servicio creado",
                Toast.LENGTH_SHORT).show();
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        listen = new SensorListen();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Servicio detenido", Toast.LENGTH_SHORT).show();
        final Intent intent = new Intent(getBaseContext(), Dialog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        sm.unregisterListener(listen);
        super.onDestroy();
        Log.d("Servicio", "servicio detenido");
    }

    @Override
    public int onStartCommand(Intent i, int flags, int idArranque) {
        Log.d("Examen", "Servicio lanzado");
        sm = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        listen = new SensorListen();

        listaSensores = sm.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        if (listaSensores.size() != 0) {
            sm.registerListener(listen, listaSensores.get(0), SensorManager.SENSOR_DELAY_UI);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public double modulo (float x, float y, float z) {
        double modulo = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        return modulo;
    }

    public class SensorListen implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                double mod = modulo(event.values[0], event.values[1], event.values[2]);
                Log.d("caida", "modulo: " + mod);
                if (mod > 25) {

                    Log.d("Caida", "Se ha detectado una caída");

                    servicioCaidas.stopSelf();

                }
            }

        }

    }

}
