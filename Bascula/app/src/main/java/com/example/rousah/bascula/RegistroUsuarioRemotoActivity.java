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

public class RegistroUsuarioRemotoActivity extends Activity {

    private Button guardar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_usuario_remoto);



        final Intent intent = new Intent(this, DoneActivity.class);

        guardar = (Button) findViewById(R.id.guardar_id);
        guardar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(intent);
                finish();

            }
        });

    }}
