package com.example.rousah.bascula;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TabPrimero extends Fragment {
    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_primero,null);



        String userUid = usuario.getUid();

        Calendar calendarNow = new GregorianCalendar(TimeZone.getTimeZone("Europe/Madrid"));
        int monthDay =calendarNow.get(Calendar.DAY_OF_MONTH);
        int month = calendarNow.get(Calendar.MONTH);
        int year = calendarNow.get(Calendar.YEAR);

        String fecha = String.valueOf(monthDay)+"-"+String.valueOf(month)+"-"+String.valueOf(year);
        ultimosDatos(fecha, userUid);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios").document(String.valueOf(Uid)).collection("mediciones").document(f).get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                //Log.w(TAG, "Se han recogido los datos.");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w(TAG, e);
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
                                    Double imc = task.getResult().getDouble("imc");

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
                                        Toast.makeText(getContext(), "No hay datos.", 0).show();// TODO Auto-generated method stub

                                    }
                                    else
                                    {
                                        TextView pesoReal = view.findViewById(R.id.pesoValor);
                                        pesoReal.setText(task.getResult().getDouble("peso").toString() + "Kg");
                                        TextView alturaReal = view.findViewById(R.id.alturaValor);
                                        alturaReal.setText(task.getResult().getDouble("temperatura").toString() + "M");
                                        TextView imcReal = view.findViewById(R.id.imcValor);
                                        imcReal.setText(task.getResult().getDouble("sensaciontermica").toString() + "");

                                    }
                                    //

                                } else {
                                    //Log.e(TAG, "Error al leer", task.getException());
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w(TAG, e);
                            }
                        }
                );

    }

    //--------------datos reales bascula-------------
    public void ultimosDatos(final String f, String Uid)
    {
    }
    //--------------datos reales bascula-------------
}