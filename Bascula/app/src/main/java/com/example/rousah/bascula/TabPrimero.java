package com.example.rousah.bascula;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.comun.Mqtt;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.firestore.DocumentSnapshot;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class TabPrimero extends Fragment implements MqttCallback {

    private Button alertButton;
    private TextView alertTextView;

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

        try {
            Log.i(Mqtt.TAG, "Suscrito a " + Mqtt.topicRoot + "PRESENCIA");
            client.subscribe(Mqtt.topicRoot + "PRESENCIA", Mqtt.qos);
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

        String fecha = String.valueOf(monthDay)+"-"+String.valueOf(month)+"-"+String.valueOf(year);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //--------------datos reales bascula-------------
        db.collection("usuarios").document(String.valueOf(userUid)).collection("mediciones").document(fecha).get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                //Log.w(TAG, "Se han recogido los datos.");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w(TAG, e);
                            }
                        }
                )
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @SuppressLint({"RestrictedApi", "WrongConstant"})
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                                if (task.isSuccessful()) {
                                    Double peso = task.getResult().getDouble("peso");
                                    Double altura = task.getResult().getDouble("altura");
                                    Double imc = task.getResult().getDouble("imc");
                                    /**
                                     *
                                     * Sí los datos recogidos son igual a null saldrá un TOAST
                                     * advirtiendo de que ese día no contiene datos.
                                     *
                                     * Sí los datos recogidos no son null saldrá un TOAST
                                     * advirtiendo del día seleccionado, y visualizará los datos
                                     * recogidos.
                                     *
                                     */
                                    if(peso == null)
                                    {
//                                        Toast.makeText(getContext(), "No hay datos.", 0).show();// TODO Auto-generated method stub
                                    }
                                    else
                                    {
                                        TextView pesoReal = view.findViewById(R.id.pesoValor);
                                        pesoReal.setText(task.getResult().getDouble("peso").toString() + " Kg");
                                        String hey = task.getResult().getDouble("peso").toString();
                                        TextView alturaReal = view.findViewById(R.id.alturaValor);
                                        alturaReal.setText(task.getResult().getDouble("altura").toString() + " M");
                                        TextView imcReal = view.findViewById(R.id.imcValor);
                                        imcReal.setText(task.getResult().getDouble("imc").toString());
                                    }
                                } else {
                                    //Log.e(TAG, "Error al leer", task.getException());
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w(TAG, e);
                            }
                        }
                );
        //--------------datos reales bascula-------------

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
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


      /*  alertButton = (Button) getView().findViewById(R.id.AlertButton);
        alertTextView = (TextView) getView().findViewById(R.id.AlertTextView);

        */

    }

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
                        (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.NOTIFICATION_SERVICE);
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
                        (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.NOTIFICATION_SERVICE);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setCancelable(true);
                builder.setTitle("ALERTA");
                builder.setMessage("FUGA DE GAS!!");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertTextView.setVisibility(View.VISIBLE);
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