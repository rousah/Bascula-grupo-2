package com.equipodos.raspberry;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Mediciones {

    private String fecha;
    private String peso;
    private String altura;
    private String user;

    public Mediciones(String p, String a, String f, String u) {
        fecha = f;
        peso = p;
        altura = a;
        user = u;
    }



    /*public void guardarMediciones(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> datos = new HashMap<>();
        datos.put(peso, altura, user);
        db.collection("medicion").document(fecha).set(datos);
    }*/


}
