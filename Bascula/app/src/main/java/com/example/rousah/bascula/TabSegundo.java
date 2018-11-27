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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class TabSegundo extends Fragment {
    private View view;
    private String TAG = "MATTHEW/GTI";
    private String fecha;
    private String userUid;
    private CalendarView calendarView;
    private FirebaseUser usuario;
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
                month = month+1;
                fecha = String.valueOf(dayOfMonth)+"-"+String.valueOf(month)+"-"+String.valueOf(year);

                //Toast.makeText(getApplicationContext(), ""+fecha, 0).show();// TODO Auto-generated method stub

                lanzarListaDeDatos(fecha, userUid);
            }
        });

        return view;
    }

    /**
     * Lanzar RecyclerView de los datos del día
     *
     */
    public void lanzarListaDeDatos(final String f, String Uid)
    {

        //Log.w(TAG,"Usuario: "+Uid);
        //Log.w(TAG,"Fecha: "+f);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(String.valueOf(Uid)).collection("mediciones").document(f).get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Log.w(TAG, "Se han recogido los datos.");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, e);
                            }
                        }
                )
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @SuppressLint({"RestrictedApi", "WrongConstant"})
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                                // definimos el intent
                                Intent i = new Intent(getContext(), DatosDiaCalendario.class);
                                if (task.isSuccessful()) {
                                    Double peso = task.getResult().getDouble("peso");
                                    Double altura = task.getResult().getDouble("altura");

                                    /**
                                     *
                                     * Sí los datos recogidos son igual a null saldrá un TOAST
                                     * advirtiendo de que ese día no contiene datos.
                                     *
                                     * Sí los datos recogidos no son null saldrá un TOAST
                                     * advirtiendo del día seleccionado, y visualizará los datos
                                     * recogidos.
                                     *
                                     */
                                    if(peso == null)
                                    {
                                        Toast.makeText(getApplicationContext(), "No hay datos.", 0).show();// TODO Auto-generated method stub

                                    }
                                    else
                                    {
                                        Log.w(TAG, "Peso:" + peso);
                                        Log.w(TAG, "Altura:" + altura);
                                        /**
                                         * Le mandamos a la clase MyAdapter estos datos, con un intent
                                         * y un Bundle que los recoja
                                         */
                                        i.putExtra("peso", peso);
                                        i.putExtra("altura", altura);
                                        startActivity(i);
                                    }
                                    //

                                } else {
                                    Log.e(TAG, "Error al leer", task.getException());
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, e);
                            }
                        }
                );


    }

}
