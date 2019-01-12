package com.example.rousah.bascula;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.BounceInterpolator;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.util.Tools;
import com.db.chart.view.LineChartView;
import com.example.comun.Mqtt;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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

import java.text.DecimalFormat;

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

    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;

    public Switch luces;

    private OnFragmentInteractionListener mListener;

    //----------------MQTT---------------------
    MqttClient client;
    //----------------MQTT---------------------


    int i = 0;

    private LineChartView grafica;

    public CasaFragment() {
        // Required empty public constructor
    }

    public static CasaFragment newInstance(String param1, String param2) {
        CasaFragment fragment = new CasaFragment();
        Bundle args = new Bundle();
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
            Log.i(Mqtt.TAG, "Suscrito a " + topicRoot+"POWER");
            client.subscribe(topicRoot+"POWER", qos);
            client.setCallback(this);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al suscribir.", e);
        }
        //---------------MQTT---------------------

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.casa, container, false);
        luces = view.findViewById(R.id.switchluces);
        db.collection("usuarios").document(usuario.getUid()).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task){
                        if (task.isSuccessful()) {

                            TextView temp = view.findViewById(R.id.temp);
                            temp.setText(task.getResult().getDouble("temperatura").toString() + " ºC");

                            TextView hum = view.findViewById(R.id.hum);
                            hum.setText(task.getResult().getDouble("humedad").toString() + " %");

                            TextView termi = view.findViewById(R.id.termi);
                            termi.setText(task.getResult().getDouble("sensaciontermica").toString() + " ºC");
                        } else {
                            Log.e("Firestore", "Error al leer", task.getException());
                        }
                    }
                });



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



        final String[] labels = new String[3];
        final float[] values = new float[3];
        final Runnable mBaseAction;
        final Tooltip mTip = new Tooltip(getContext(), R.layout.tooltip, R.id.value);

        db.collection("usuarios")
                .document(usuario.getUid())
                .collection("mediciones")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(3)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                 @Override
                                                 public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                     if (task.isSuccessful()) {
                                                         int i = 0;
                                                         for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                             float peso = Float.parseFloat(documentSnapshot.getData().get("peso").toString());
                                                             String fecha = documentSnapshot.getData().get("fecha").toString();
                                                             fecha = fecha.substring(4, 10);
                                                             labels[i] = fecha;
                                                             values[i] = peso;
                                                             i++;
                                                         }
                                                         ((TextView) mTip.findViewById(R.id.value)).setTypeface(
                                                                 Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Semibold.ttf"));

                                                         mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
                                                         mTip.setDimensions((int) Tools.fromDpToPx(58), (int) Tools.fromDpToPx(25));
                                                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

                                                             mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                                                                     PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                                                                     PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

                                                             mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                                                                     PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                                                                     PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

                                                             mTip.setPivotX(Tools.fromDpToPx(65) / 2);
                                                             mTip.setPivotY(Tools.fromDpToPx(25));
                                                         }

                                                         LineSet dataset = new LineSet(labels, values);

                                                         dataset.setColor(Color.parseColor("#399699"))
                                                                 .setDotsColor(Color.parseColor("#ff869b"))
                                                                 .setThickness(4)
                                                                 .setDashed(new float[]{10f, 10f})
                                                         //  .setFill(Color.parseColor("#3d6c73"))
                                                         //  .setGradientFill(new int[]{Color.parseColor("#364d5a"), Color.parseColor("#3f7178")}, null)
                                                         ;
                                                         
                                                        /* mBaseAction = action;
                                                         Runnable chartAction = new Runnable() {
                                                             @Override
                                                             public void run() {

                                                                 mBaseAction.run();
                                                                 mTip.prepare(mChart.getEntriesArea(0).get(3), mValues[0][3]);
                                                                 mChart.showTooltip(mTip, true);
                                                             }
                                                         };

                                                         */

                                                         Paint paint = new Paint();
                                                         paint.setColor(Color.parseColor("#E3E3E3"));
                                                         DecimalFormat formato = new DecimalFormat();
                                                         formato.applyPattern("#0.0");
                                                         grafica = view.findViewById(R.id.linechart);
                                                         grafica.addData(dataset);
                                                         grafica.setAxisColor(Color.parseColor("#399699"))
                                                                 .setYAxis(false)
                                                                 .setYLabels(AxisRenderer.LabelPosition.NONE)
                                                                 .setGrid(10, 20, paint)
                                                                 .setAxisBorderValues(0, 100)
                                                                 .setTypeface(Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Semibold.ttf"))
                                                                 .setLabelsFormat(formato)
                                                                 .setTooltips(mTip)
                                                                 .show(new Animation().setInterpolator(new BounceInterpolator())
                                                                         .fromAlpha(0));

                                                         grafica.show();
                                                     }
                                                 }
                                             });


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
        Log.d(Mqtt.TAG, "Recibiendo: " + topic + "->" + payload);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (payload.contains("ON")) {
                    luces.setChecked(true);
                    Toast.makeText(getContext(), "Luces encendidas", Toast.LENGTH_SHORT).show();
                    Log.d(Mqtt.TAG, "encendiendo");
                }
                if (payload.contains("OFF")) {
                    luces.setChecked(false);
                    Toast.makeText(getContext(), "Luces apagadas", Toast.LENGTH_SHORT).show();
                    Log.d(Mqtt.TAG, "apagando");
                }
            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
