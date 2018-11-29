package com.example.rousah.bascula;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CasaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CasaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CasaFragment extends Fragment implements MqttCallback {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private View view;

    public Switch luces;

    private OnFragmentInteractionListener mListener;

    //----------------MQTT---------------------
    MqttClient client;
    //----------------MQTT---------------------

    public CasaFragment() {
        // Required empty public constructor
    }

    public static CasaFragment newInstance(String param1, String param2) {
        CasaFragment fragment = new CasaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //---------------MQTT---------------------
        try {
            Log.i(Mqtt.TAG, "Conectando al broker " + broker);
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topicRoot+"WillTopic", "App desconectada".getBytes(),
                    qos, false);
            client.connect(connOpts);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al conectar.", e);
        }

        try {
            Log.i(Mqtt.TAG, "Suscrito a " + topicRoot+"cmnd/POWER");
            client.subscribe(topicRoot+"cmnd/POWER", qos);
            client.setCallback(this);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al suscribir.", e);
        }
        //---------------MQTT---------------------
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.casa, container, false);
        luces = view.findViewById(R.id.switchluces);

        // AQUI PONER CODIGO PARA LEER EL ESTADO MAS RECIENTE DEL SONOFF
        // Y CAMBIAR EL ESTADO DEL SWITCH LUCES ACORDE CON EL ESTADO DEL
        // SONOFF


        // Este código crea un bucle discoteca encendiendo y apagando el sonoff sin parar
       /* luces.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.w("switch: ", "pruebas");
                if (isChecked) {
                    try {
                        Log.i(Mqtt.TAG, "Publicando mensaje: " + "encender sonoff");
                        MqttMessage message = new MqttMessage("ON".getBytes());
                        message.setQos(qos);
                        message.setRetained(false);
                        client.publish(topicRoot + "cmnd/POWER", message);
                        Toast.makeText(getContext(), "Luces encendidas", Toast.LENGTH_SHORT).show();
                    } catch (MqttException e) {
                        luces.setChecked(false);
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error", e);
                    }
                }
                if (!isChecked) {
                    try {
                        Log.i(Mqtt.TAG, "Publicando mensaje: " + "apagar sonoff");
                        MqttMessage message = new MqttMessage("OFF".getBytes());
                        message.setQos(qos);
                        message.setRetained(false);
                        client.publish(topicRoot + "cmnd/POWER", message);
                        Toast.makeText(getContext(), "Luces apagadas", Toast.LENGTH_SHORT).show();
                    } catch (MqttException e) {
                        luces.setChecked(true);
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error", e);
                    }
                }
            }
        }); */

        //---------------MQTT---------------------
        luces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (luces.isChecked()) {
                    try {
                        Log.i(Mqtt.TAG, "Publicando mensaje: " + "encender sonoff");
                        MqttMessage message = new MqttMessage("ON".getBytes());
                        message.setQos(qos);
                        message.setRetained(false);
                        client.publish(topicRoot + "cmnd/POWER", message);
                        Toast.makeText(getContext(), "Luces encendidas", Toast.LENGTH_SHORT).show();
                    } catch (MqttException e) {
                        luces.setChecked(false);
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error", e);
                    }
                }
                if (!luces.isChecked()) {
                    try {
                        Log.i(Mqtt.TAG, "Publicando mensaje: " + "apagar sonoff");
                        MqttMessage message = new MqttMessage("OFF".getBytes());
                        message.setQos(qos);
                        message.setRetained(false);
                        client.publish(topicRoot + "cmnd/POWER", message);
                        Toast.makeText(getContext(), "Luces apagadas", Toast.LENGTH_SHORT).show();
                    } catch (MqttException e) {
                        luces.setChecked(true);
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error", e);
                    }
                }
            }
        });
        //---------------MQTT---------------------

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d("Internet: ", cause.getMessage());
        MainActivity estado = (MainActivity)getActivity();
        Log.d("internet", cause.getMessage());
        while (!((MainActivity)getActivity()).isNetworkAvailable()) {
            Log.d(TAG, "Reintentando conexión MQTT");
            try {
                Log.i(Mqtt.TAG, "Conectando al broker " + broker);
                client = new MqttClient(broker, clientId, new MemoryPersistence());
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setKeepAliveInterval(60);
                connOpts.setWill(topicRoot+"WillTopic", "App desconectada".getBytes(),
                        qos, false);
                client.connect(connOpts);
            } catch (MqttException e) {
                Log.e(Mqtt.TAG, "Error al conectar.", e);
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        final String payload = new String(message.getPayload());
        Log.d(TAG, "Recibiendo: " + topic + "->" + payload);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                luces = view.findViewById(R.id.switchluces);
                if (payload.contains("ON")) {
                    luces.setChecked(true);
                    Toast.makeText(getContext(), "Luces encendidas", Toast.LENGTH_SHORT).show();
                }
                if (payload.contains("OFF")) {
                    luces.setChecked(false);
                    Toast.makeText(getContext(), "Luces apagadas", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d("ajjaajjaja", token.toString());
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
