package com.example.rousah.bascula;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class DatosSemanales extends Activity {

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
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
        Double i = extras.getDouble("imc");

        // Llenamos una lista que pasamos al Adapter
        ArrayList<Double> listaDatos = new ArrayList<Double>();
        listaDatos.add(0, p);
        listaDatos.add(1, a);
        listaDatos.add(2, i);



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

    }

}
