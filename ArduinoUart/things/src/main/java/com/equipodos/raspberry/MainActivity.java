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
        // Primero comprobamos que el M5Stack esté conectado para recibir los datos
        // que las ESP32 le envien
        Log.d(TAG, "Hay conexión?");
        Log.d(TAG, "Mandado a Arduino: 1");
        // Mandando 1 comprueba la conexión
        uart.escribir("1");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Log.w(TAG, "Error en sleep()", e);
        }
        String s1 = uart.leer();
        Log.d(TAG, "Recibido de Arduino: "+s1);

        // En caso de estar CONECTADO le enviará al M5, un 2 para que le devuelva
        // los datos recogidos tanto de la báscula como de los ESP32

        switch (s1){
            case "CONECTADO":
                uart.escribir("2");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Error en sleep()", e);
                }
                String s2 = uart.leer();
                Log.d(TAG, "Recibido de Arduino: "+s2);
                break;
        }
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
