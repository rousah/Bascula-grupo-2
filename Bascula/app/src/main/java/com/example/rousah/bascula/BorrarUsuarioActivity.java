package com.example.rousah.bascula;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class BorrarUsuarioActivity extends Activity {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.borrar_user);

        Button si = findViewById(R.id.siButton);
        si.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lanzarBorrar();
            }
        });
        Button no = findViewById(R.id.noButton);
        no.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lanzarNoBorrar();
            }
        });
    }

    public void lanzarBorrar(){
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(BorrarUsuarioActivity.this, "Su cuenta ha sido borrada", Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut(); //End user session
                            startActivity(new Intent(BorrarUsuarioActivity.this, LoginActivity.class)); //Go back to home page
                            BorrarUsuarioActivity.this.finish();
                        }
                    }
                });

    }

    public void lanzarNoBorrar(){
        Intent i = new Intent(BorrarUsuarioActivity.this, MainActivity.class);
        startActivity(i);
    }
}
