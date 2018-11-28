package com.example.rousah.bascula;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CrearPerfil extends AppCompatActivity {
    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Context context = this;

    private RadioGroup radioGroup;
    private RadioButton radioButtonSelected;
    int selectedId;
    private String proveedor;

    Calendar myCalendar = Calendar.getInstance();

    EditText fecha;

    //variables necesarias para guardar la imagen en firebase
    private Uri filePath;
    private static final int SOLICITUD_PERMISO_GALERIA = 5;
    private ImageView imagenPerfil;
    private Button elegirImagen;
    private String uid;

    private Uri uriStorage;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.crear_perfil);

        TextView nombre = findViewById(R.id.nombreCrearPerfil);
        nombre.setText(usuario.getDisplayName());

        TextView email = findViewById(R.id.emailCrearPerfil);
        email.setText(usuario.getEmail());

        elegirImagen = (Button) findViewById(R.id.elegirButton);

        imagenPerfil = findViewById(R.id.fotoCrearPerfil);


        //miramos si tiene la imagen
        comprobarImagen();

        fecha = findViewById(R.id.fechaNac);

        elegirImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escogerImagen();
            }
        });
    }

    private void escogerImagen() {
        if(proveedor.equals("google.com")) {
            Toast.makeText(CrearPerfil.this, "No se deben cambiar imagenes de google+", Toast.LENGTH_LONG).show();
        }else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SOLICITUD_PERMISO_GALERIA);
        }
    }
    //recibimos el request del startActivityfor...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SOLICITUD_PERMISO_GALERIA && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imagenPerfil.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    //comprueba si el user tiene imagen en storage, si no la tiene comprueba en google, si no pondra una por defecto
    private void comprobarImagen(){
        //variables: imagen en Storage, uid del user actual y el proveedor de google
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        proveedor = usuario.getProviders().get(0);

        if(storageReference.child("usuarios/"+uid+"/imagenUsuario.jpg") != null){
            storageReference.child("usuarios/"+uid+"/imagenUsuario.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    //Toast.makeText(CrearPerfil.this, uri.toString(), Toast.LENGTH_LONG).show();
                    Picasso.with(CrearPerfil.this).load(uri.toString())
                            .transform(new CircleTransform())
                            .into(imagenPerfil);
                    System.out.println("dentro de getPhoto");
                }
            });
        }else if(proveedor.equals("google.com")) {
            Log.d("FOTO GOOGLE", usuario.getPhotoUrl().toString());
            String uri2 = usuario.getPhotoUrl().toString();
            //Para cargar la foto en mejor calidad
            uri2 = uri2.replace("/s96-c/","/s300-c/");
            //Usa transform para hacerla circular
            Picasso.with(this).load(uri2)
                    .transform(new CircleTransform())
                    .into(imagenPerfil);
            Toast.makeText(CrearPerfil.this, "Tu imagen es de google", Toast.LENGTH_LONG).show();

            System.out.println("dentro de getPhoto");
        }
        //por si se logea con email y no tiene foto asignada
        else {
            //   imagenPerfil.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_account_circle_black_55dp, null));
            Picasso.with(this).load(R.drawable.round_account_circle_black_48dp).transform(new CircleTransform()).into(imagenPerfil);
            Toast.makeText(CrearPerfil.this, "No tienes imagen", Toast.LENGTH_LONG).show();

        }
    }

    private void guardarImagen() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference dataRef = storageReference.child("usuarios/"+uid+"/imagenUsuario.jpg");
            dataRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(CrearPerfil.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CrearPerfil.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
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
            Toast.makeText(CrearPerfil.this, "Complete todos los campos", Toast.LENGTH_LONG).show();
        }
        else {
            guardarImagen();
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

