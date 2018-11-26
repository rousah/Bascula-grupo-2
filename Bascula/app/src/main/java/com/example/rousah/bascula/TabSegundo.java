package com.example.rousah.bascula;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class TabSegundo extends Fragment {
    View view;
    CalendarView calendarView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);



    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_segundo, container, false);

        /**
         * Recoge el ID del calendario en el que interactuamos
         */
        CalendarView calendarView =(CalendarView) view.findViewById(R.id.calendarView);
        /**
         *
         */
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @SuppressLint({"WrongConstant", "RestrictedApi"})
            @Override
            /**
             * Función que recoge el día/mes/año seleccionado
             */
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                String fecha = String.valueOf(dayOfMonth)+"-"+String.valueOf(month)+"-"+String.valueOf(year);

                Toast.makeText(getApplicationContext(), ""+fecha, 0).show();// TODO Auto-generated method stub

                lanzarLista(fecha);
            }
        });

       /* Button button = view.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                lanzarLista();
            }
        });*/


        return view;
    }

    /**
     * Lanzar RecyclerView de los días
     *
     */
    public void lanzarLista(String f)
    {
        Log.w("Fecha", f);
        Intent i = new Intent(getContext(), DatosDiaCalendario.class);
        startActivity(i);
    }

}
