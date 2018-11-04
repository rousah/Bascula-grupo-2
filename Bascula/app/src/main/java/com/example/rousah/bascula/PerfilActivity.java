package com.example.rousah.bascula;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import static com.firebase.ui.auth.ui.email.RegisterEmailFragment.TAG;

public class PerfilActivity extends Activity {

    private DatabaseReference db;
    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    //FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil);

        db = FirebaseDatabase.getInstance().getReference();

        EditText nombre = findViewById(R.id.nombre);

        nombre.setText(usuario.getDisplayName());

    }


    /*void mostrarDatos(final FirebaseUser user) {

        EditText nombre = findViewById(R.id.nombre);

        db.collection("usuarios").document(user.getUid()).addSnapshotListener(
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e){
                        if (e != null) {
                            Log.e("Firebase", "Error al leer", e);
                        } else if (snapshot == null || !snapshot.exists()) {
                            Log.e("Firebase", "Error: documento no encontrado ");
                        } else {
                            Log.e("Firestore", "datos:" + snapshot.getData());
                        }
                    }
                });

        //nombre.setText(db.collection("usuarios").document(user.getUid()).get().addOnCanceledListener();

    }*/
}
