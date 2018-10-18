package com.example.rousah.bascula;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RemoveRemoteCheckActivity extends Activity {
    private TextView mEdit;
    private Button aceptar;
    private Button rechazar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remove_remote_check_activity);


        Bundle receivedData = getIntent().getExtras();
        String nombre = receivedData.getString("nombre");

        TextView fieldTextView = (TextView) findViewById(R.id.textView2);

        fieldTextView.setText("Estas seguro que quieres borrar a\n" + nombre );

        aceptar = (Button) findViewById(R.id.aceptar);
        aceptar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("resultado", "Aceptado");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        rechazar = (Button) findViewById(R.id.rechazar);
        rechazar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("resultado", "Rechazado");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
