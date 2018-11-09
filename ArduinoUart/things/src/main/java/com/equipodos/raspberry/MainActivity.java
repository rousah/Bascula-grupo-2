package com.equipodos.raspberry;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.protobuf.Timestamp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.CallableStatement;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private String leido;
    private String s;
    private JSONObject obj;
    private JSONArray array;
    private String peso;
    private String altura;
    private String fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "Lista de UART disponibles: " + ArduinoUart.disponibles());
        ArduinoUart uart = new ArduinoUart("UART0", 115200);


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.w(TAG, "Error en sleep()", e);
        }


        while(true){
            //
            leido = uart.leer();

            switch (leido) {
                case "S":

                    uart.escribir("2");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Error en sleep()", e);
                    }
                    s = uart.leer();

                    Log.d(TAG, "Subiendo datos...");
                    Log.d(TAG, s);

                    /*try {
                        obj = new JSONObject(s);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        peso = obj.getString("Peso");
                        altura = obj.getString("Altura");
                        fecha = obj.getString("ID");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    */

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> datos = new HashMap<>();
                    datos.put("Peso", 2);
                    datos.put("Altura", 2);

                    db.collection("medicion").document("1").set(datos);


                    // si se suben los datos
                    //if(){
                    //uart.escribir("OK");
                    //}



                    break;
            }


        }






    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
