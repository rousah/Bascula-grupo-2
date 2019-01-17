package com.example.rousah.bascula;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class DatosDiaCalendario extends Activity{

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private String tipo;
    private Button cerrar;
    private String TAG = "EQUIPO2/GTI";
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.datos_un_dia_cal);

        cerrar = findViewById(R.id.cerrar);

        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = findViewById(R.id.recycler_view_measurement);

        /**
         * Recogemos los datos de la clase TabSegundo
         * @params fecha, peso, altura, imc
         */

        // Llenamos una lista que pasamos al Adapter
        ArrayList<Double> listaDatos = new ArrayList<Double>();
        Bundle extras = getIntent().getExtras();
        tipo = extras.getString("tipo");

        if(tipo == "varios")
        {
            Double f = extras.getDouble("fecha");
            Double p = extras.getDouble("peso");
            Double a = extras.getDouble("altura");
            Double i = extras.getDouble("imc");

            //listaDatos.add(0, tipo);
            listaDatos.add(0, f);
            listaDatos.add(1, p);
            listaDatos.add(2, a);
            listaDatos.add(3, i);
        }else
        {
            Double p = extras.getDouble("peso");
            Double a = extras.getDouble("altura");
            Double i = extras.getDouble("imc");
            Log.d(TAG, "peso"+p);

            //listaDatos.add(0, tipo);
            listaDatos.add(0, p);
            listaDatos.add(1, a);
            listaDatos.add(2, i);
        }

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
        mAdapter = new MyAdapter(listaDatos, getBaseContext());
        mRecyclerView.setAdapter(mAdapter);

    }
}