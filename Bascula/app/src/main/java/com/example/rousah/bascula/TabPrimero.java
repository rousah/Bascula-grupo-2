package com.example.rousah.bascula;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comun.Mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static com.example.comun.Mqtt.broker;
import static com.example.comun.Mqtt.clientId;
import static com.example.comun.Mqtt.qos;
import static com.example.comun.Mqtt.topicRoot;
import static com.firebase.ui.auth.AuthUI.TAG;

public class TabPrimero extends Fragment implements MqttCallback {

    private Button alertButton;
    private TextView alertTextView;


    //----------------MQTT---------------------
    MqttClient client;
    //----------------MQTT---------------------


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
            Log.i(Mqtt.TAG, "Suscrito a " + topicRoot + "alarma");
            client.subscribe(topicRoot + "alarma", qos);
            client.setCallback((MqttCallback) this);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al suscribir.", e);
        }
        //---------------MQTT---------------------
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tab_primero, null);
        return v;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


        alertButton = (Button) getView().findViewById(R.id.AlertButton);
        alertTextView = (TextView) getView().findViewById(R.id.AlertTextView);

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
                if (payload.contains("ALERTA_DE_GAS")) {
                    alertaGas();
                }
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