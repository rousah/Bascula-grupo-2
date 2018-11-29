package com.example.rousah.bascula;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;

import com.example.comun.Mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.comun.Mqtt.broker;
import static com.example.comun.Mqtt.clientId;
import static com.example.comun.Mqtt.qos;
import static com.example.comun.Mqtt.topicRoot;
import static com.firebase.ui.auth.AuthUI.TAG;


import static android.support.v4.content.ContextCompat.getSystemService;

public class TabSegundo extends Fragment implements MqttCallback{


    //----------------MQTT---------------------
    MqttClient client;
    //----------------MQTT---------------------

    private NotificationManager notificationManager;
    static final String CANAL_ID = "mi_canal";
    static final int NOTIFICACION_ID = 1;

    CalendarView calendarView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //---------------MQTT---------------------
        try {
            Log.i(Mqtt.TAG, "Conectando al broker " + broker);
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topicRoot + "WillTopic", "App desconectada".getBytes(),
                    qos, false);
            client.connect(connOpts);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al conectar.", e);
        }

        try {
            Log.i(Mqtt.TAG, "Suscrito a " + topicRoot + "puerta");
            client.subscribe(topicRoot + "puerta", qos);
            client.setCallback((MqttCallback) this);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al suscribir.", e);
        }
        //---------------MQTT---------------------


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_segundo, container, false);
    }


    @Override
    public void connectionLost(Throwable cause) {

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        final String payload = new String(message.getPayload());
        Log.d(TAG, "Recibiendo: " + topic + "->" + payload);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (payload.contains("Puerta_Abierta")) {
                    notificacionPuerta();
                }
            }

            private void notificacionPuerta(){
                notificationManager = getSystemService(NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(
                            CANAL_ID, "Mis Notificaciones",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    notificationChannel.setDescription("Bienvenido a casa");
                    notificationManager.createNotificationChannel(notificationChannel);
                }
                NotificationCompat.Builder notificacion =
                        new NotificationCompat.Builder(getActivity(), CANAL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Saludos")
                                .setContentText("Bienvenido a casa :D");
                notificationManager.notify(NOTIFICACION_ID, notificacion.build());
            }


        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}
