package com.example.rousah.bascula;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CrearPerfil extends AppCompatActivity {
    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Context context = this;

    private RadioGroup radioGroup;
    private RadioButton radioButtonSelected;
    int selectedId;

    Calendar myCalendar = Calendar.getInstance();

    EditText fecha;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.crear_perfil);

        TextView nombre = findViewById(R.id.nombreCrearPerfil);
        nombre.setText(usuario.getDisplayName());

        TextView email = findViewById(R.id.emailCrearPerfil);
        email.setText(usuario.getEmail());

        final ImageView imagenPerfil = findViewById(R.id.fotoCrearPerfil);
        String proveedor = usuario.getProviders().get(0);
        //checkea si el proveedor es de google por si se logea con un email
        if(proveedor.equals("google.com")) {
            String uri = usuario.getPhotoUrl().toString();
            //carga la foto y usa transform para hacerla circular
            Picasso.with(this).load(uri).transform(new CircleTransform()).into(imagenPerfil);
            System.out.println("dentro de getPhoto");
        }
        //por si se logea con email y no tiene foto asignada
        else {
            //   imagenPerfil.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_account_circle_black_55dp, null));
            Picasso.with(this).load(R.drawable.round_account_circle_black_48dp).transform(new CircleTransform()).into(imagenPerfil);
        }
        fecha = findViewById(R.id.fechaNac);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        fecha.setText(sdf.format(myCalendar.getTime()));
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    public void seleccionaFecha (View view) {
            new DatePickerDialog(this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    public void guardar(View view) {

        radioGroup = findViewById(R.id.radioSexo);
        EditText telefono = findViewById(R.id.telefonoCrear);
        selectedId = radioGroup.getCheckedRadioButtonId();
        radioButtonSelected = (RadioButton) findViewById(selectedId);

        Log.w("perfil: fecha", fecha.getText().toString());
        Log.w("perfil: tlf", telefono.getText().toString());
        if (telefono.getText().toString().equals("") || fecha.getText().toString().equals("dd/mm/yy") || radioButtonSelected == null) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_LONG).show();
        }
        else {
            selectedId = radioGroup.getCheckedRadioButtonId();
            radioButtonSelected = (RadioButton) findViewById(selectedId);
            Map<String, Object> datos = new HashMap<>();

            datos.put("telefono", telefono.getText().toString());
            datos.put("sexo", radioButtonSelected.getTag());
            datos.put("fechaNac", fecha.getText().toString());

            final DocumentReference usuarioActual = db.collection("usuarios").document(usuario.getUid());
            usuarioActual.update(datos)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Succes firestore", "DocumentSnapshot added with ID: " + usuarioActual.getId());
                    final Intent iMain = new Intent(context, MainActivity.class);
                    startActivity(iMain);

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Error firestore", "Error adding document", e);
                        }
                    });
        }
    }
}

