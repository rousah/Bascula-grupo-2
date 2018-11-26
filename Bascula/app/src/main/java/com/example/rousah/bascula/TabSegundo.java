package com.example.rousah.bascula;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class TabSegundo extends Fragment {
    View view;
    String fecha;
    String userUid;
    CalendarView calendarView;
    FirebaseUser usuario;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        userUid = usuario.getUid();

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

                fecha = String.valueOf(dayOfMonth)+"-"+String.valueOf(month)+"-"+String.valueOf(year);

                Toast.makeText(getApplicationContext(), ""+fecha, 0).show();// TODO Auto-generated method stub

                lanzarLista();
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
     * Lanzar RecyclerView de los datos del día
     *
     */
    public void lanzarLista()
    {

        Log.w("USUARIO:", userUid);
        Log.w("Fecha", fecha);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(String.valueOf(userUid)).collection("mediciones").document(fecha).get()
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                                if (task.isSuccessful()) {
                                    //String peso = task.getResult().getString("dato_1");
                                    //String altura = task.getResult().getString("altura");
                                    //Log.w("Firestore", "Peso:" + peso + " Altura:" + altura);
                                    Log.w("Firestore", "Peso:" + task.getResult().getString("peso"));
                                } else {
                                    Log.e("Firestore", "Error al leer", task.getException());
                                }
                            }
                        });
        Intent i = new Intent(getContext(), DatosDiaCalendario.class);
        startActivity(i);
    }

}
