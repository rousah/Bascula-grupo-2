package com.example.rousah.bascula;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.util.Log;

import java.util.ArrayList;

public class CalendarDay extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datos_un_dia_cal);
        mRecyclerView = findViewById(R.id.recycler_view_measurement);

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
         * Asociamos el RacyclerView con un adapter, este
         * renderiza la información que tenemos.
         *
         * El adapter está configurado en la clase MyAdapter
         */
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);

    }

    /* @Override
    public void onResponse(Call<ArrayList<MyAdapter>> call, Response<ArrayList<MyAdapter>> response)
    {
        if(response.isSuccesful())
        {
            ArrayList<MyAdapter> myAdapter = response.body();
            Log.d("onResponse myAdapter", "Size of myAdapter => " + myAdapter.size());
            mAdapter.setDataSet(myAdapter);
        }
    }*/

}
