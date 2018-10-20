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

public class RemoveRemoteCheckActivity extends Activity {

    private Button aceptar;
    private Button rechazar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remove_remote_check_activity);

        Intent intent =  getIntent();
        String usuarioremoto = intent.getStringExtra("usuarioremoto");

        TextView fieldTextView = (TextView) findViewById(R.id.confirmacion);

        fieldTextView.setText(usuarioremoto );

        final Intent intent2 = new Intent(this, DoneActivity.class);

        aceptar = (Button) findViewById(R.id.aceptar);
        aceptar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(intent2);
                finish();

            }
        });

        final Intent intent3 = new Intent(this, MainActivity.class);

        rechazar = (Button) findViewById(R.id.rechazar);
        rechazar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(intent3);
                finish();

            }
        });
    }
}
