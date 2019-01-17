package com.equipodos.raspberry;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.things.pio.Gpio;
import com.google.firebase.firestore.DocumentReference;
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
    private static final String TAG = "MATTHEW/GTI";
    private String leido;
    private String s;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<String, Object> datos = new HashMap<>();
    private JSONObject obj;
    private JSONArray array;
    private String peso;
    private String altura;
    private String fecha;
    private ArduinoUart uart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "Lista de UART disponibles: " + ArduinoUart.disponibles());
        uart = new ArduinoUart("UART0", 115200);




        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.w(TAG, "Error en sleep()", e);
        }


        try {
            uart.boton();
        } catch (IOException e) {
            Log.w(TAG, "Unable to access UART device", e);
        }


    }

    public void guardarFirestore(FirebaseFirestore db, String c, String d, String n, String v ){

        datos.put(n, v);
        db.collection(c).document(d).set(datos);

    }

    public void recibidoUart (String leido) {
        Log.d(TAG, "llegado recibido");
        switch (leido) {
            // Muestra en el logcat
            case "M":
                uart.escribir("2");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Error en sleep()", e);
                }
                s = uart.leer();

                Log.d(TAG, "Datos recogidos: ");
                Log.d(TAG, s);
                break;
            // Sube los datos a firestore
            case "S":

                uart.escribir("2");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Error en sleep()", e);
                }
                s = uart.leer();

                Log.d(TAG, "Datos recogidos: ");
                Log.d(TAG, s);


                try {
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

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Error en sleep()", e);
                }


                guardarFirestore(db, "mediciones", fecha, "peso", peso);

                Log.d(TAG, "Subiendo datos...");

                break;

        }
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
