package com.example.rousah.bascula;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PerfilActivity extends Activity {

    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

    @Override public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil);

        mostrarDatos(usuario);

    }

    public void editarUsuario(View view) {
        view.setVisibility(View.GONE);

        Button guardar = findViewById(R.id.save);
        guardar.setVisibility(View.VISIBLE);

        Button cancelar = findViewById(R.id.cancel);
        cancelar.setVisibility(View.VISIBLE);

        TextView nombre = findViewById(R.id.nombre);
        nombre.setVisibility(View.GONE);

        EditText nombreEditable = findViewById(R.id.nombre_editable);
        nombreEditable.setVisibility(View.VISIBLE);
    }

    void mostrarDatos(final FirebaseUser usuario) {
        TextView nombre = findViewById(R.id.nombre);
        nombre.setText(usuario.getDisplayName());

        EditText email = findViewById(R.id.email);
        email.setText(usuario.getEmail());

        EditText numero = findViewById(R.id.telefono);
        numero.setText(usuario.getPhoneNumber());
    }


}
