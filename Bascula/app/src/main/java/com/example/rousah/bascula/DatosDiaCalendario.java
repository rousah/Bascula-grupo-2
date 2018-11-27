package com.example.rousah.bascula;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class DatosDiaCalendario extends Activity {

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private TextView valorPeso;
    private TextView valorAltura;
    private String TAG = "EQUIPO2/GTI";
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.datos_un_dia_cal);

        mRecyclerView = findViewById(R.id.recycler_view_measurement);

        /**
         * Recogemos los datos de la clase TabSegundo
         * @params peso, altura
         */
        Bundle extras = getIntent().getExtras();

        Double p = extras.getDouble("peso");
        Double a = extras.getDouble("altura");

        ArrayList<Double> listaDatos = new ArrayList<Double>();
        listaDatos.add(0, p);
        listaDatos.add(1, a);



        //Log.w(TAG, "peso"+s);

        /**
         * Usar esta línea para mejorar el rendimiento si
         * sabemos que el contenido no va a afectar el tamaño
         * del recyclerview.
         *
         * Sirve para darle el mismo tamaño a los objetos que hay
         * en el RecyclerView.
         */
        mRecyclerView.setHasFixedSize(true);

        /**
         * El RecyclerView utilizará un LinearLayoutManager,
         * se lo asociamos al RecyclerView, la otra alternativa
         * es en cuadricula, nosotros lo hacemos en líneas.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        /**
         * Asociamos el RecyclerView con un adapter, este
         * renderiza la información que tenemos.
         *
         * El adapter está configurado en la clase MyAdapter
         */
        mAdapter = new MyAdapter(listaDatos);
        mRecyclerView.setAdapter(mAdapter);

        /*Button next = (Button) findViewById(R.id.volverAlCalendario);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }

        });*/


    }

}