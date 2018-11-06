//  BORJA
/*
VERSION: V 1.0.0
DESCRIPTION:
Initial
 */

package com.example.rousah.bascula;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UsuarioRemotoActivity extends Activity {

    private Button reseleccionar;
    private Button eliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usuario_remoto);

        Intent intent =  getIntent();
        String usuarioremoto = intent.getStringExtra("usuarioremoto");


        setContentView(R.layout.usuario_remoto);

         TextView fieldTextView = (TextView) findViewById(R.id.usuario);

         fieldTextView.setText(usuarioremoto);


        final Intent intent2 = new Intent(this, UsuariosRemotosActivity.class);

        reseleccionar = (Button) findViewById(R.id.reseleccionar);
        reseleccionar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(intent2);
                finish();

            }
        });

        final Intent intent3 = new Intent(this, RemoveRemoteCheckActivity.class);
        intent3.putExtra("usuarioremoto", usuarioremoto);

        eliminar = (Button) findViewById(R.id.eliminar);
        eliminar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            startActivity(intent3);
                finish();

            }
        });
    }

}
