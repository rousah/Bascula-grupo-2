package com.example.rousah.bascula;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.comun.Mqtt;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class TabPrimero extends Fragment implements MqttCallback {

    private Button alertButton;
    private TextView alertTextView;
    /**
     *  ---------------------- NEARBY CONNECTIONS
     *  ------f---------------- PARAMETERS
     *
     */
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION=1;
    // Consejo: utiliza como SERVICE_ID el nombre de tu paquete
    private static final String SERVICE_ID = "com.equipodos.nearbyconnections";
    private static final String TAG = "Mobile:";
    String endpointId;

    Button botonConnect;
    Button botonON;
    /**
     * --------------------------------------------------------------------
     */
    //private TextView textview;
    //----------------MQTT---------------------
    MqttClient client;
    //----------------MQTT---------------------

    public static String telefonoEmergencia = "";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //---------------MQTT---------------------
        try {
            Log.i(Mqtt.TAG, "Conectando al broker " + Mqtt.broker);
            client = new MqttClient(Mqtt.broker, Mqtt.clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(Mqtt.topicRoot + "WillTopic", "App desconectada".getBytes(),
                    Mqtt.qos, false);
            client.connect(connOpts);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al conectar.", e);
        }

        try {
            Log.i(Mqtt.TAG, "Suscrito a " + Mqtt.topicRoot + "alarma");
            client.subscribe(Mqtt.topicRoot + "alarma", Mqtt.qos);
            client.setCallback((MqttCallback) this);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al suscribir.", e);
        }


        //---------------MQTT---------------------


    }


    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tab_primero,null);
        String userUid = usuario.getUid();
        Calendar calendarNow = new GregorianCalendar(TimeZone.getTimeZone("Europe/Madrid"));
        int monthDay =calendarNow.get(Calendar.DAY_OF_MONTH);
        int month = calendarNow.get(Calendar.MONTH) + 1;
        int year = calendarNow.get(Calendar.YEAR);

        /**
         * NEARBY CONNECTIONS
         */
        // Botón conexión
        botonConnect = view.findViewById(R.id.button3);
        botonConnect.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                // Iniciamos la conexión con al anunciante "Nearby LED"
                Log.i(TAG, "Conectando...");
                Toast.makeText(getApplicationContext(), "Conectando...", Toast.LENGTH_SHORT).show();
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
                                //textview.setText("Desconectado");
                                Log.i(TAG, "Desconectado");
                                Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Comprobación de permisos peligrosos
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }else
        {
            startDiscovery();
        }

        String fecha = String.valueOf(monthDay)+"-"+String.valueOf(month)+"-"+String.valueOf(year);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //--------------llamada-------------

        db.collection("usuarios").document(usuario.getUid()).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task){
                        if (task.isSuccessful()) {

                            telefonoEmergencia = task.getResult().getString("telefonoEm");

                        } else {
                            Log.e("Firestore", "Error al leer", task.getException());
                        }
                    }
                });
        //--------------llamada-------------

        //--------------datos reales bascula-------------
        //db.collection("usuarios").document(String.valueOf(userUid)).collection("mediciones").document(fecha).get()
        db.collection("usuarios").document(String.valueOf(userUid)).collection("mediciones")
                .orderBy("fecha", Query.Direction.DESCENDING).limit(1).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 0;
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                float peso = Float.parseFloat(documentSnapshot.getData().get("peso").toString());
                                float altura = Float.parseFloat(documentSnapshot.getData().get("altura").toString());

                                /*TextView textoPeso = view.findViewById(R.id.pesoValor);
                                if(pref.getString("peso","?") == "1") {
                                    textoPeso.setText(String.valueOf(peso) + " Kg");
                                }else{

                                    textoPeso.setText(String.valueOf(peso) + " Libras");
                                }
                                TextView textoAltura = view.findViewById(R.id.alturaValor);


                                if(pref.getString("altura","?") == "1") {
                                    textoAltura.setText(String.valueOf(altura) + " Metros");
                                }else{

                                    textoPeso.setText( altura + " Pies");
                                }*/

                                SharedPreferences pref =
                                        PreferenceManager.getDefaultSharedPreferences(getContext());
                                String s = pref.getString("peso","?");
                                if(pref.getString("peso","?").equals("1")){
                                    TextView pesoReal = view.findViewById(R.id.pesoValor);
                                    pesoReal.setText(String.valueOf(peso) + " " + getResources().getString(R.string.Kg));
                                }else{
                                    TextView pesoReal = view.findViewById(R.id.pesoValor);
                                    Float x = 2.2f;
                                    peso = peso * x;
                                    String y = String.valueOf(peso);
                                    y = String.format("%.2f", peso);
                                    pesoReal.setText( y + " " + getResources().getString(R.string.Libras));
                                }

                                if(pref.getString("altura","?").equals("1")) {
                                    TextView alturaReal = view.findViewById(R.id.alturaValor);
                                    alturaReal.setText(String.valueOf(altura) + " " + getResources().getString(R.string.Metros));
                                }else{
                                    TextView alturaReal = view.findViewById(R.id.alturaValor);
                                    Float x = 3.2f;
                                    altura = altura * x;
                                    String y = String.valueOf(altura);
                                    y = String.format("%.2f", altura);
                                    alturaReal.setText(String.valueOf(altura) + " " + getResources().getString(R.string.Pies));
                                }


                                TextView textoImc = view.findViewById(R.id.imcValor);
                                float imc = Float.parseFloat(documentSnapshot.getData().get("imc").toString());
                                textoImc.setText(String.valueOf(imc));


                                String fecha = documentSnapshot.getData().get("fecha").toString();
                                fecha = fecha.substring(4, 10);
                                i++;
                            }

                        }

                    }
                });
    //--------------datos reales bascula-------------
        return view;
    }

    /**
     * NEARBY CONNECTIONS
     */
    // Gestión de permisos
    @Override public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permisos concedidos");
                } else {
                    Log.i(TAG, "Permisos denegados");
                    //textview.setText("Debe aceptar los permisos para comenzar");
                    Log.i(TAG, "Debe aceptar los permisos para comenzar.");
                    //botonSearch.setEnabled(false);
                }
                return;
            }
        }
    }
    public void startDiscovery() {
        Nearby.getConnectionsClient(getContext()).startDiscovery(SERVICE_ID,
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
        Nearby.getConnectionsClient(getContext()).stopDiscovery();
        Log.i(TAG, "Se ha detenido el modo descubrimiento.");
    }
    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override public void onEndpointFound(String deviceId,
                                                      DiscoveredEndpointInfo discoveredEndpointInfo) {
                    Log.i(TAG, "Descubierto dispositivo con Id: " + deviceId);
                    //textview.setText("Descubierto: " + discoveredEndpointInfo
                    //.getEndpointName());
                    Log.i(TAG, "Descubierto:"+ discoveredEndpointInfo.getEndpointName());
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
                            //textview.setText("Conectado");
                            Log.i(TAG,"Conectado");
                            String Uid = usuario.getUid() ;
                            sendData(endpointId, Uid);
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.i(TAG, "Conexión rechazada por uno o ambos lados");
                            //textview.setText("Desconectado");
                            Log.i(TAG,"Desconectado");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.i(TAG, "Conexión perdida antes de poder ser " +
                                    "aceptada");
                            //textview.setText("Desconectado");
                            Log.i(TAG,"Desconectado");
                            break;
                    }
                }
                @Override public void onDisconnected(String endpointId) {
                    Log.i(TAG, "Desconexión del endpoint, no se pueden " +
                            "intercambiar más datos.");
                    //textview.setText("Desconectado");
                    Log.i(TAG,"Desconectado");
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
        //textview.setText("Transfiriendo...");
        Log.i(TAG,"Transfiriendo...");
        Payload data = null;
        try {
            data = Payload.fromBytes(mensaje.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error en la codificación del mensaje.", e);
        }
        Nearby.getConnectionsClient(getContext()).sendPayload(endpointId, data);
        Log.i(TAG, "Mensaje enviado.");
    }
    /**
     * NEARBY CONNECTIONS -----------------------------------------------------
     */
    /**
     * -----------------------------------------------------------------------------------------
     */
    /**
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


      /*  alertButton = (Button) getView().findViewById(R.id.AlertButton);
        alertTextView = (TextView) getView().findViewById(R.id.AlertTextView);

        */

    }
    /**
     *
     * @param cause
     */
    @Override
    public void connectionLost(Throwable cause) {

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        final String payload = new String(message.getPayload());
        Log.d(Mqtt.TAG, "Recibiendo: " + topic + "->" + payload);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (payload.contains("ALERTA_DE_GAS")) {
                    alertaGas();
                }
                if(payload.contains("IN")){
                    notificacionDentro();
                }
                if(payload.contains("OUT")){
                    notificacionFuera();
                }
            }

            private void notificacionFuera() {
                NotificationManager mNotificationManager =
                        null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    mNotificationManager = (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.NOTIFICATION_SERVICE);
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("default",
                            "NOMBRE_DEL_CANAL",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("DESCRIPCION_DEL_CANAL");
                    mNotificationManager.createNotificationChannel(channel);
                }



                @SuppressLint("RestrictedApi") NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                        .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                        .setContentTitle("Hasta pronto") // title for notification
                        .setContentText("Que pase un buen dia")// message for notification
                        .setAutoCancel(true); // clear notification after click
                @SuppressLint("RestrictedApi") Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                @SuppressLint("RestrictedApi") PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                mNotificationManager.notify(0, mBuilder.build());
            }

            private void notificacionDentro() {
                NotificationManager mNotificationManager =
                        null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    mNotificationManager = (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.NOTIFICATION_SERVICE);
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("default",
                            "NOMBRE_DEL_CANAL",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("DESCRIPCION_DEL_CANAL");
                    mNotificationManager.createNotificationChannel(channel);
                }



                @SuppressLint("RestrictedApi") NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                        .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                        .setContentTitle("Bienvenido") // title for notification
                        .setContentText("Bienvenido a casa")// message for notification
                        .setAutoCancel(true); // clear notification after click
                @SuppressLint("RestrictedApi") Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                @SuppressLint("RestrictedApi") PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                mNotificationManager.notify(0, mBuilder.build());
            }

            private void alertaGas() {

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());

                builder.setCancelable(true);
                builder.setTitle("ALERTA");
                builder.setMessage("FUGA DE GAS!!");
                builder.setIcon(R.drawable.ic_danger);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                     //   alertTextView.setVisibility(View.VISIBLE);
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}