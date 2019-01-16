package com.equipodos.raspberry;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
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
    private static final String TAG = MainActivity.class.getSimpleName();
    private String leido;
    private String s;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<String, Object> datos = new HashMap<>();
    private JSONObject obj;
    private JSONArray array;
    private String peso;
    private String altura;
    private String fecha;
    /**
     * NEARBY CONNECTIONS
     */
    // Consejo: utiliza como SERVICE_ID el nombre de tu paquete
    private static final String SERVICE_ID = "com.equipodos.nearbyconnections";
    private static final String TAGT = "Things:";
    private final String PIN_LED = "BCM18";
    public Gpio mLedGpio;
    private Boolean ledStatus;
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

        /**
         * nearby connections
         */
        // Configuración del LED
        ledStatus = false;
        PeripheralManager service = PeripheralManager.getInstance();try {
            mLedGpio = service.openGpio(PIN_LED);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAGT, "Error en el API PeripheralIO", e);
        }
        // Arrancamos modo anunciante
        startAdvertising();

        while(true){
            //
            leido = uart.leer();

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



                    
                    Log.d(TAG, "Subiendo datos...");

                    break;

            }

        }






    }

    public void guardarFirestore(FirebaseFirestore db, String c, String d, String n, String v ){

        datos.put(n, v);
        db.collection(c).document(d).set(datos);

    }

        private void startAdvertising() {
            Nearby.getConnectionsClient(getBaseContext()).startAdvertising(
                    "Nearby LED", SERVICE_ID, mConnectionLifecycleCallback,
                    new AdvertisingOptions(Strategy.P2P_STAR))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override public void onSuccess(Void unusedResult) {
                            Log.i(TAGT, "Estamos en modo anunciante!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override public void onFailure(@NonNull Exception e) {
                            Log.e(TAGT, "Error al comenzar el modo anunciante", e);
                        }
                    });
        }
        private void stopAdvertising() {
            Nearby.getConnectionsClient(this).stopAdvertising();
            Log.i(TAGT, "Detenido el modo anunciante!");
        }
        private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
                new ConnectionLifecycleCallback() {
                    @Override public void onConnectionInitiated(
                            String endpointId, ConnectionInfo connectionInfo) {
        // Aceptamos la conexión automáticamente en ambos lados.
                        Nearby.getConnectionsClient(getApplicationContext())
                                .acceptConnection(endpointId, mPayloadCallback);
                        Log.i(TAGT, "Aceptando conexión entrante sin autenticación");
                    }
                    @Override public void onConnectionResult(String endpointId,
                                                             ConnectionResolution result) {
                        switch (result.getStatus().getStatusCode()) {
                            case ConnectionsStatusCodes.STATUS_OK:
                                Log.i(TAGT, "Estamos conectados!");
                                stopAdvertising();
                                break;
                            case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                                Log.i(TAGT, "Conexión rechazada por uno o ambos lados");
                                break;
                            case ConnectionsStatusCodes.STATUS_ERROR:
                                Log.i(TAGT, "Conexión perdida antes de ser aceptada");
                                break;
                        }
                    }
                    @Override
                    public void onDisconnected(String endpointId) {
                        Log.i(TAGT, "Desconexión del endpoint, no se pueden " +
                                "intercambiar más datos.");
                        startAdvertising();
                    }
                };
        private final PayloadCallback mPayloadCallback = new PayloadCallback() {
            @Override public void onPayloadReceived(String endpointId,
                                                    Payload payload) {
                String message = new String(payload.asBytes());
                Log.i(TAGT, "Se ha recibido una transferencia desde (" +
                        endpointId + ") con el siguiente contenido: " + message);
                disconnect(endpointId);
                switch (message) {
                    case "SWITCH":
                        switchLED();
                        break;
                    default:
                        Log.w(TAGT, "No existe una acción asociada a este " +
                                "mensaje.");
                        break;
                }
            }
            @Override public void onPayloadTransferUpdate(String endpointId,PayloadTransferUpdate update) {
                // Actualizaciones sobre el proceso de transferencia
            }
        };
        public void switchLED() {
            try {
                if (ledStatus) {
                    mLedGpio.setValue(false);
                    ledStatus = false;
                    Log.i(TAGT, "LED OFF");
                } else {
                    mLedGpio.setValue(true);
                    ledStatus = true;
                    Log.i(TAGT, "LED ON");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error en el API PeripheralIO", e);
            }
        }
        protected void disconnect(String endpointId) {
            Nearby.getConnectionsClient(this)
                    .disconnectFromEndpoint(endpointId);
            Log.i(TAGT, "Desconectado del endpoint (" + endpointId + ").");
            startAdvertising();
        }
        @Override
        protected void onDestroy() {
            super.onDestroy();
            stopAdvertising();
            if (mLedGpio != null) {
                try {
                    mLedGpio.close();
                } catch (IOException e) {
                    Log.e(TAGT, "Error en el API PeripheralIO", e);
                } finally {
                    mLedGpio = null;
                }
            }
        }
    }