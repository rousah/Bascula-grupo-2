package com.example.rousah.bascula;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

import static com.example.rousah.bascula.Usuarios.guardarUsuario;

public class LoginActivity  extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        login();
    }

    private void login() {
        final FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario != null) {
            Toast.makeText(this, "inicia sesión: " +
                    usuario.getDisplayName() + " - " + usuario.getEmail() + " - " +
                    usuario.getProviders().get(0), Toast.LENGTH_LONG).show();

            final Intent iPerfil = new Intent(this, CrearPerfil.class);
            iPerfil.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            final Intent iMain = new Intent(this, MainActivity.class);
            iMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            db.collection("usuarios").document(usuario.getUid()).get().addOnCompleteListener(
                    new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task){
                            if (task.isSuccessful()) {
                                if (task.getResult().getString("telefono")==null) {
                                    guardarUsuario(usuario);
                                    startActivity(iPerfil);
                                }
                                else {
                                    startActivity(iMain);
                                }
                            } else {
                                Log.e("Firestore", "Error al leer", task.getException());
                            }
                        }
                    });
        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()

                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(true)
                                    .build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build())) //<<<<<<<<<<<<<
                    .setIsSmartLockEnabled(false)
                    .setTheme(R.style.LoginTheme)
                    .setLogo(R.drawable.logo_login)
                    .build(), RC_SIGN_IN);
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                login();
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "Sin conexión a Internet",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(this, "Error desconocido",
                        Toast.LENGTH_LONG).show();
                Log.e("Autentificación", "Sign-in error: ", response.getError());
            }
        }
    }
}