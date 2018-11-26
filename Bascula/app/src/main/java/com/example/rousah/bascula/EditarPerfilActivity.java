package com.example.rousah.bascula;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditarPerfilActivity extends MainActivity{
    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Context context = this;

    private RadioGroup radioGroup;
    private RadioButton radioButtonSelected;
    int selectedId;

    Calendar myCalendar = Calendar.getInstance();

    EditText fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_perfil);

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

}

