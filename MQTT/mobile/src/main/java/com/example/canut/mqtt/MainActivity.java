package com.example.canut.mqtt;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static com.example.comun.Mqtt.*;


public class MainActivity extends AppCompatActivity {

    MqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.i(TAG, "Publicando mensaje: " + "hola");
                    MqttMessage message = new MqttMessage("hola".getBytes());
                    message.setQos(qos);
                    message.setRetained(false);
                    client.publish(topicRoot+"saludo", message);
                } catch (MqttException e) {
                    Log.e(TAG, "Error al publicar.", e);
                }
                Snackbar.make(view, "Publicando en MQTT", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        });


        try {
            Log.i(TAG, "Conectando al broker " + broker);
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            client.connect();
        } catch (MqttException e) {
            Log.e(TAG, "Error al conectar.", e);
        }

    }
    @Override public void onDestroy() {
        try {
            Log.i(TAG, "Desconectado");
            client.disconnect();
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar.", e);
        }
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
