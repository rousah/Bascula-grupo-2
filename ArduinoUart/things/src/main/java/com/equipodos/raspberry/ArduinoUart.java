package com.equipodos.raspberry;

import android.os.Handler;
import android.support.annotation.NonNull;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ArduinoUart {
    private static final String TAG = "MATTHEW/GTI";
    private MainActivity mainActivity;

    private UartDevice uart;
    // If set, contains a stored exception to throw when user asks for data
    private IOException mLastException;
    private JSONObject objMedidas;
    private JSONObject objCasa;
    private Double peso;
    private Double altura;
    private Double t;
    private Double h;
    private Double calor;
    private String texto;
    private String casa;
    private Map<String, Object> datos = new HashMap<>();
    //Para leer del serial cada 5 segundos
    final int duracion = 7000;
    final Handler handler = new Handler();


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
                    //Log.d(TAG, "dentro del callback");

                    /*Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            try{

                                casa = leer();
                                Log.d(TAG, casa);
                                objCasa = new JSONObject(casa);
                                t = objCasa.getDouble("Temperatura");
                                h = objCasa.getDouble("Humedad");
                                calor = objCasa.getDouble("Calor");


                            }
                            catch(Exception e){
                                // En caso de error
                            }
                            finally{
                                // ejecutamos otra vez el handlers

                                handler.postDelayed(this, duracion);
                            }
                        }
                    };

                    handler.postDelayed(runnable, duracion);*/

                    texto = leer();
                    //Log.d(TAG, texto);
                    if (texto.equals("S")) {

                        //Log.d(TAG, "llamado recibido");



                        escribir("2");

                        Calendar c = Calendar.getInstance();
                        int month = c.get(Calendar.MONTH)+1;
                        String date = c.get(Calendar.DAY_OF_MONTH)+"-"+month+"-"+c.get(Calendar.YEAR);
                        //Log.d(TAG, date);

                        Date fecha = new Date();
                        Timestamp fechaStamp = new Timestamp(fecha);

                        try {

                            Thread.sleep(1000);
                            //Log.d(TAG, "Sleep");
                        } catch (InterruptedException e) {

                            Log.w(TAG, "Error en sleep()", e);

                        }
                        String s = leer();

                        //Log.d(TAG, "Datos recogidos: ");
                        //Log.d(TAG, s);

                        try {
                            objMedidas = new JSONObject(s);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            peso = objMedidas.getDouble("Peso");
                            altura = objMedidas.getDouble("Altura");
                            t = objMedidas.getDouble("Temperatura");
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
                        fecha = String.valueOf(monthDay)+"-"+String.valueOf(month)+"-"+String.valueOf(year);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        datos.put("fecha", fecha);
                        datos.put("peso", peso);
                        datos.put("altura", altura);
<<<<<<< HEAD
                        datos.put("temperatura", t);
                        //datos.put("humedad", h);
                        //datos.put("calor", calor);


                        db.collection("mediciones").document(date).set(datos)
=======

                        db.collection("mediciones").document(fecha).set(datos)
>>>>>>> 7ca49cd81bd334e9d38722f57e8e28d69fe5b430
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
        String v = "";

        int len;

        final int maxCount = 8; // Máximo de datos leídos cada vez

        byte[] buffer = new byte[maxCount];

        try {
            do {
                len = uart.read(buffer, buffer.length);

                for (int i=0; i<len; i++) {
                    v += (char)buffer[i];
                }

            } while(len>0);
        } catch (IOException e) {
            Log.w(TAG, "Error al leer de UART", e);
        }

        return v;
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