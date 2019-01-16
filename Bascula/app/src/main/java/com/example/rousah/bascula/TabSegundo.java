package com.example.rousah.bascula;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatViewInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.joaquimley.faboptions.FabOptions;

import org.xmlpull.v1.XmlPullParser;

import static com.example.rousah.bascula.R.layout.faboptions_button;
import static com.example.rousah.bascula.R.layout.semanal;
import static com.example.rousah.bascula.R.layout.tab_segundo;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class TabSegundo extends Fragment implements View.OnClickListener{
    private View view;
    private String TAG = "MATTHEW/GTI";
    private String fecha;
    private String userUid;
    private FirebaseUser usuario;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        userUid = usuario.getUid();
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_segundo, null);

        FabOptions fabOptions = view.findViewById(R.id.fab_options);
        fabOptions.setButtonsMenu(R.menu.menu_filter);

        fabOptions.setBackgroundColor(getContext(), getResources().getColor(R.color.colorPrimaryDark));
        fabOptions.setFabColor(R.color.colorAccent);

        fabOptions.setButtonColor(R.id.faboptions_seven, R.color.white);
        fabOptions.setButtonColor(R.id.faboptions_mes, R.color.white);
//        fabOptions.setButtonColor(R.id.faboptions_trimestral, R.color.white);
        fabOptions.setButtonColor(R.id.faboptions_anual, R.color.white);

        fabOptions.setOnClickListener(this);

        /**
         * Recoge el ID del calendario en el que interactuamos
         */
        CalendarView calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        /**
         *
         */
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @SuppressLint({"WrongConstant", "RestrictedApi"})
            @Override
            /**
             * Función que recoge el día/mes/año seleccionado
             */
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                month = month + 1;
                fecha = String.valueOf(dayOfMonth) + "-" + String.valueOf(month) + "-" + String.valueOf(year);

                //Toast.makeText(getApplicationContext(), ""+fecha, 0).show();// TODO Auto-generated method stub

                lanzarListaDeDatosDeUnDia(fecha, userUid);
            }
        });

        return view;
    }

    /**
     * Lanzar RecyclerView de los datos del día
     *
     * Busca los datos del día seleccionado.
     * Peso, altura, IMC...
     *
     */
    /**
     * @param f
     * @param Uid
     */
    public void lanzarListaDeDatosDeUnDia(final String f, String Uid) {

        //Log.w(TAG,"Usuario: "+Uid);
        //Log.w(TAG,"Fecha: "+f);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(String.valueOf(Uid)).collection("mediciones").document(f).get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Log.w(TAG, "Se han recogido los datos.");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, e);
                            }
                        }
                )
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @SuppressLint({"RestrictedApi", "WrongConstant"})
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                // definimos el intent
                                Intent i = new Intent(getContext(), DatosDiaCalendario.class);
                                if (task.isSuccessful()) {
                                    Double peso = task.getResult().getDouble("peso");
                                    Double altura = task.getResult().getDouble("altura");
                                    Double imc = task.getResult().getDouble("imc");

                                    /**
                                     *
                                     * Sí los datos recogidos son igual a null saldrá un TOAST
                                     * advirtiendo de que ese día no contiene datos.
                                     *
                                     * Sí los datos recogidos no son null saldrá un TOAST
                                     * advirtiendo del día seleccionado, y visualizará los datos
                                     * recogidos.
                                     *
                                     */
                                    if (peso == null) {
                                        Toast.makeText(getApplicationContext(), "No hay datos.", 0).show();// TODO Auto-generated method stub

                                    } else {
                                        Log.w(TAG, "Peso:" + peso);
                                        Log.w(TAG, "Altura:" + altura);
                                        Log.w(TAG, "IMC: " + imc);
                                        /**
                                         * Le mandamos a la clase MyAdapterGlobalOptions estos datos, con un intent
                                         * y un Bundle que los recoja
                                         */
                                        i.putExtra("tipo", "uno");
                                        i.putExtra("peso", peso);
                                        i.putExtra("altura", altura);
                                        i.putExtra("imc", imc);

                                        startActivity(i);
                                    }
                                    //

                                } else {
                                    Log.e(TAG, "Error al leer", task.getException());
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, e);
                            }
                        }
                );


    }

    /**
     * Lanzar recyclerview datos de la semana visualizando gráfica con el intervalo
     * del mes.
     *
     * @param f1
     * @param Uid
     */
    public void lanzarSemanal(String f1, String Uid) {

        FirebaseDatabase.getInstance().getReference().child("usuarios").child(Uid).child("mediciones").
                orderByValue().startAt(f1).limitToLast(7);
        Intent s = new Intent(getContext(), MyAdapterGlobalOptions.class);
        startActivity(s);
    }

    /**
     * Lanzar recyclerview datos del mes visualizando gráfica con el intervalo
     * del mes.
     *
     * @param view
     */
    public void lanzarMensual(View view) {
        Intent m = new Intent(getContext(), MyAdapterGlobalOptions.class);
        startActivity(m);
    }

    /**
     * Lanzar recyclerview datos del trimestre visualizando gráfica con el intervalo
     * del trimestre.
     *
     * @param view
     */
    public void lanzarTrimestral(View view) {
        Intent t = new Intent(getContext(), MyAdapterGlobalOptions.class);
        startActivity(t);
    }

    /**
     * Lanzar recyclerview datos del año visualizando gráfica con el intervalo
     * del año.
     *
     * @param view
     */
    public void lanzarAnual(View view) {
        Intent a = new Intent(getContext(), Anual.class);
        startActivity(a);
    }



    /*

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_filter, menu);

    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();

        if(itemId == R.id.faboptions_seven)
        {
            Toast.makeText(getContext(), "SEMANAL", 0).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(R.string.elegir).setTitle(R.string.title_f);

            builder.setView(R.layout.dialog_fecha);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User click OK button
                    //lanzarSemanal(fecha, Uid);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User click CANCEL button
                }
            });

            AlertDialog dialog = builder.create();

            dialog.show();

        }else if(itemId == R.id.faboptions_mes)
        {
            Toast.makeText(getContext(), "MENSUAL", 0).show();
            lanzarMensual(null);
        }else if(itemId == R.id.faboptions_trimestral)
        {
            Toast.makeText(getContext(), "TRIMESTRAL", 0).show();
            lanzarTrimestral(null);
        }else if(itemId == R.id.faboptions_anual)
        {
            Toast.makeText(getContext(), "ANUAL", 0).show();
            lanzarAnual(null);
        }else
        {
            Toast.makeText(getContext(), "ERROR NO LLEVA A NINGÚN SITIO", 0).show();
        }

        return super.onOptionsItemSelected(item);
    }

*/

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.faboptions_anual:
                Toast.makeText(getContext(), "ANUAL", Toast.LENGTH_SHORT).show();
                lanzarAnual(null);
                break;
            case R.id.faboptions_seven:
                Toast.makeText(getContext(), "SEMANAL", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage(R.string.elegir).setTitle(R.string.title_f);

                builder.setView(R.layout.dialog_fecha);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User click OK button
                        //lanzarSemanal(fecha, Uid);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User click CANCEL button
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.show();
                break;
            case R.id.faboptions_mes:
                Toast.makeText(getContext(), "MENSUAL", Toast.LENGTH_SHORT).show();
                lanzarMensual(null);
                break;
            case R.id.faboptions_trimestral:
                Toast.makeText(getContext(), "TRIMESTRAL", Toast.LENGTH_SHORT).show();
                lanzarTrimestral(null);
                break;
            default:
        }

    }
}
