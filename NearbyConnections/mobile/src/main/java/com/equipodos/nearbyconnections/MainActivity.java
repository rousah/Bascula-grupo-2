package com.equipodos.nearbyconnections;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION=1;
    // Consejo: utiliza como SERVICE_ID el nombre de tu paquete
    private static final String SERVICE_ID = "com.equipodos.nearbyconnections";
    private static final String TAG = "Mobile:";
    String endpointId;
    Button botonSearch;
    Button botonConnect;
    Button botonON;
    TextView textview;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview = (TextView) findViewById(R.id.textView1);
        botonSearch = (Button) findViewById(R.id.buttonLED);
        botonSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Boton presionado");
                startDiscovery();
                textview.setText("Buscando...");
            }
        });
        // Botón conexión
        botonConnect = findViewById(R.id.button2);
        botonConnect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Iniciamos la conexión con al anunciante "Nearby LED"
                Log.i(TAG, "Conectando...");
                Nearby.getConnectionsClient(getApplicationContext())
                        .requestConnection("Nearby LED",endpointId,
                                mConnectionLifecycleCallback)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override public void onSuccess(Void unusedResult) {
                                Log.i(TAG, "Solicitud lanzada, falta que ambos " +"lados acepten");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error en solicitud de conexión", e);
                                textview.setText("Desconectado");
                            }
                        });
            }
        });
        // ON LED
        botonON = findViewById(R.id.button5);
        botonON.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

            }
        });
// Comprobación de permisos peligrosos
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
    }
    // Gestión de permisos
    @Override public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permisos concedidos");
                } else {
                    Log.i(TAG, "Permisos denegados");
                    textview.setText("Debe aceptar los permisos para comenzar");
                    botonSearch.setEnabled(false);
                }
                return;
            }
        }
    }
    private void startDiscovery() {
        Nearby.getConnectionsClient(this).startDiscovery(SERVICE_ID,
                mEndpointDiscoveryCallback, new DiscoveryOptions(Strategy.P2P_STAR))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override public void onSuccess(Void unusedResult) {
                        Log.i(TAG, "Estamos en modo descubrimiento!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Modo descubrimiento no iniciado.", e);
                    }
                });
    }
    private void stopDiscovery() {
        Nearby.getConnectionsClient(this).stopDiscovery();
        Log.i(TAG, "Se ha detenido el modo descubrimiento.");
    }
    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override public void onEndpointFound(String deviceId,
                                                      DiscoveredEndpointInfo discoveredEndpointInfo) {
                    Log.i(TAG, "Descubierto dispositivo con Id: " + deviceId);
                    textview.setText("Descubierto: " + discoveredEndpointInfo
                            .getEndpointName());
                    endpointId = deviceId;
                    stopDiscovery();
                }
                @Override public void onEndpointLost(String endpointId) {}
            };
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
// Aceptamos la conexión automáticamente en ambos lados.
                    Log.i(TAG, "Aceptando conexión entrante sin autenticación");
                    Nearby.getConnectionsClient(getApplicationContext())
                            .acceptConnection(endpointId, mPayloadCallback);
                }
                @Override public void onConnectionResult(String endpointId,
                                                         ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.i(TAG, "Estamos conectados!");
                            textview.setText("Conectado");
                            sendData(endpointId, "SWITCH");
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.i(TAG, "Conexión rechazada por uno o ambos lados");
                            textview.setText("Desconectado");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.i(TAG, "Conexión perdida antes de poder ser " +
                                    "aceptada");
                            textview.setText("Desconectado");
                            break;
                    }
                }
                @Override public void onDisconnected(String endpointId) {
                    Log.i(TAG, "Desconexión del endpoint, no se pueden " +
                            "intercambiar más datos.");
                    textview.setText("Desconectado");
                }
            };
    private final PayloadCallback mPayloadCallback = new PayloadCallback() {// En este ejemplo, el móvil no recibirá transmisiones de la RP3
        @Override public void onPayloadReceived(String endpointId,
                                                Payload payload) {
// Payload recibido
        }
        @Override public void onPayloadTransferUpdate(String endpointId,
                                                      PayloadTransferUpdate update) {
// Actualizaciones sobre el proceso de transferencia
        }
    };
    private void sendData(String endpointId, String mensaje) {
        textview.setText("Transfiriendo...");
        Payload data = null;
        try {
            data = Payload.fromBytes(mensaje.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error en la codificación del mensaje.", e);
        }
        Nearby.getConnectionsClient(this).sendPayload(endpointId, data);
        Log.i(TAG, "Mensaje enviado.");
    }
}
