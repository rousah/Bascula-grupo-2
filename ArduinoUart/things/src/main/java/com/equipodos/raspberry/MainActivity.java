package com.equipodos.raspberry;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "Lista de UART disponibles: " + ArduinoUart.disponibles());
        ArduinoUart uart = new ArduinoUart("UART0", 115200);
        Log.d(TAG, "Hay conexión?");
        Log.d(TAG, "Mandado a Arduino: 1");
        uart.escribir("1");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Log.w(TAG, "Error en sleep()", e);
        }
        String s1 = uart.leer();
        Log.d(TAG, "Recibido de Arduino: "+s1);

        if(s1 == "CONECTADO"){
            uart.escribir("2");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Log.w(TAG, "Error en sleep()", e);
            }
            String s2 = uart.leer();
            Log.d(TAG, "Recibido de Arduino: "+s2);
        }else{
            String s3 = uart.leer();
            Log.d(TAG, "Recibido de Arduino: "+s3);
        }

        //Log.d(TAG, s);
        /*FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> datos = new HashMap<>();
        datos.put(peso, altura);
        db.collection("medicion").document("id"+fecha).set(datos);*/


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
