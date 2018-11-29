package com.equipodos.raspberry;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Handler;

import static android.content.ContentValues.TAG;

public class ArduinoUart {
    private static final String TAG = "MATTHEW/GTI";
    private MainActivity mainActivity;

    private UartDevice uart;
    // If set, contains a stored exception to throw when user asks for data
    private IOException mLastException;
    private JSONObject obj;
    private JSONArray array;
    private String peso;
    private String altura;
    private String fecha;
    String texto;
    private Map<String, Object> datos = new HashMap<>();



    public ArduinoUart(String nombre, int baudios) {
            try {
                uart = PeripheralManager.getInstance().openUartDevice(nombre);
                uart.setBaudrate(baudios);
                uart.setDataSize(8);
                uart.setParity(UartDevice.PARITY_NONE);
                uart.setStopBits(1);
            } catch (IOException e) {
                Log.w(TAG, "Error iniciando UART", e);
            }
        }
        public void escribir(String s) {
            try {
                int escritos = uart.write(s.getBytes(), s.length());
                Log.d(TAG, escritos+" bytes escritos en UART");
            } catch (IOException e) {
                Log.w(TAG, "Error al escribir en UART", e);
            }
        }


        public void boton () throws IOException {

            // Keep an exception, in case data is requested before it is available;
            mLastException = new IOException("No data available");

            android.os.Handler mHandler = new android.os.Handler();
            UartDeviceCallback mUartCallback = new UartDeviceCallback() {
                @Override
                public boolean onUartDeviceDataAvailable(UartDevice uart) {
                    // Read available data from the UART device
                    Log.d(TAG, "dentro del callback");

                    texto = leer();

                    Log.d(TAG, texto);

                    if (texto.equals("S")) {
                        Log.d(TAG, "llamado recbido");
                        escribir("2");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Log.w(TAG, "Error en sleep()", e);
                        }
                        String s = leer();

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

                        Calendar calendarNow = new GregorianCalendar(TimeZone.getTimeZone("Europe/Madrid"));
                        int monthDay =calendarNow.get(Calendar.DAY_OF_MONTH);
                        int month = calendarNow.get(Calendar.MONTH);
                        int year = calendarNow.get(Calendar.YEAR);

                        Log.d(TAG, String.valueOf(monthDay)+"-"+String.valueOf(month)+"-"+String.valueOf(year));

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        datos.put("peso", peso);
                        datos.put("altura", altura);
                        datos.put("randomizer", Math.random());

                        db.collection("mediciones uarto").document(fecha).set(datos)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "subido");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "fail");
                            }
                        });
                       // Log.d(TAG, "Subiendo datos...");
                    }

                    // Continue listening for more interrupts
                    return true;
                }

                @Override
                public void onUartDeviceError(UartDevice uart, int error) {
                    Log.w(TAG, uart + ": Error event " + error);
                }
            };

            uart.registerUartDeviceCallback(mHandler, mUartCallback);


        }


    public String leer() {
        String s = "";

        int len;
        final int maxCount = 8; // Máximo de datos leídos cada vez
        byte[] buffer = new byte[maxCount];
        try {
            do {
                len = uart.read(buffer, buffer.length);
                for (int i=0; i<len; i++) {
                    s += (char)buffer[i];
                }
            } while(len>0);
        } catch (IOException e) {
            Log.w(TAG, "Error al leer de UART", e);
        }
        return s;
    }
    public void cerrar() {
        if (uart != null) {
            try {
                uart.close();
                uart = null;
            } catch (IOException e) {
                Log.w(TAG, "Error cerrando UART", e);
            }
        }
    }
    static public List<String> disponibles() {
        return PeripheralManager.getInstance().getUartDeviceList();
    }


}