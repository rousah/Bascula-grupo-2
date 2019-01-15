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
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.example.rousah.bascula.R.layout.faboptions_button;
import static com.example.rousah.bascula.R.layout.semanal;
import static com.example.rousah.bascula.R.layout.tab_segundo;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class TabSegundo extends Fragment {
    private View view;
    private String TAG = "MATTHEW/GTI";
    private String fecha;
    private String userUid;
    private Button ok;
    private FirebaseUser usuario;
    private Calendar myCalendar;
    private EditText fLabel;
    private LayoutInflater inflater;
    private View dialogView;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        userUid = usuario.getUid();
        setHasOptionsMenu(true);


        //Asignación variables para la view del dialogo de filtros
        inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_fecha, null);
        fLabel = dialogView.findViewById(R.id.dialog_calendar);
        myCalendar = Calendar.getInstance();
        fLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionaFecha(null);
            }
        });
    }

    @SuppressLint({"ResourceAsColor", "ResourceType"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(tab_segundo, container, false);

        FabOptions fabOptions = view.findViewById(R.id.fab_options);
        fabOptions.setButtonsMenu(R.menu.menu_filter);

        fabOptions.setBackgroundColor(R.color.colorPrimaryDark);
        fabOptions.setFabColor(R.color.colorAccent);

        fabOptions.setButtonColor(R.id.faboptions_seven, R.color.white);
        fabOptions.setButtonColor(R.id.faboptions_mes, R.color.white);
        fabOptions.setButtonColor(R.id.faboptions_trimestral, R.color.white);
        fabOptions.setButtonColor(R.id.faboptions_anual, R.color.white);


        /**
         * Recoge el ID del calendario en el que interactuamos
         */
        CalendarView calendarView =(CalendarView) view.findViewById(R.id.calendarView);
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
                month = month+1;
                fecha = String.valueOf(dayOfMonth)+"-"+String.valueOf(month)+"-"+String.valueOf(year);

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
     *
     * @param f
     * @param Uid
     */
    public void lanzarListaDeDatosDeUnDia(final String f, String Uid)
    {

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
                            public void onComplete(@NonNull Task<DocumentSnapshot> task){
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
                                    if(peso == null)
                                    {
                                        Toast.makeText(getApplicationContext(), "No hay datos.", 0).show();// TODO Auto-generated method stub

                                    }
                                    else
                                    {
                                        Log.w(TAG, "Peso:" + peso);
                                        Log.w(TAG, "Altura:" + altura);
                                        Log.w(TAG, "IMC: "+imc);
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
     * @param
     * @param
     *
     */
    public void lanzarSemanal(String f, String user)
    {
        //Log.d(TAG, "VAYA MIERDA!");
        FirebaseDatabase.getInstance().getReference().child("usuarios").child(user).child("mediciones").
                orderByValue().startAt(f).limitToLast(7);

        //Intent s = new Intent(getContext(), MyAdapterGlobalOptions.class);
        //startActivity(s);
    }

    /**
     * Lanzar recyclerview datos del mes visualizando gráfica con el intervalo
     * del mes.
     * @param
     */
    public void lanzarMensual(String f, String user)
    {
        Intent m = new Intent(getContext(), MyAdapterGlobalOptions.class);
        startActivity(m);
    }

    /**
     * Lanzar recyclerview datos del trimestre visualizando gráfica con el intervalo
     * del trimestre.
     * @param
     */
    public void lanzarTrimestral(String f, String user)
    {
        Intent t = new Intent(getContext(), MyAdapterGlobalOptions.class);
        startActivity(t);
    }

    /**
     * Lanzar recyclerview datos del año visualizando gráfica con el intervalo
     * del año.
     * @param
     */
    public void lanzarAnual(String f, String user)
    {
        Intent a = new Intent(getContext(), MyAdapterGlobalOptions.class);
        startActivity(a);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_filter, menu);

    }

    /**
     * Funciones para calendario seleccionar último día del que quieres ver datos
     *
     * updateLabel()
     * seleccionaFecha()
     */
    private void updateLabel() {

        Log.d(TAG, "Dentro de update label");
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        Log.d(TAG,sdf.format(myCalendar.getTime()));
        //fLabel.setText(sdf.format(myCalendar.getTime()));
        fLabel.setText(sdf.format(myCalendar.getTime()));
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            Log.d(TAG, "Dentro de calendar");
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            int month = monthOfYear + 1;
            Log.d(TAG, "Fecha: "+dayOfMonth+"-"+String.valueOf(month).toString()+"-"+year);

            updateLabel();
        }

    };

    public void seleccionaFecha (View view) {

        Log.d(TAG, "Estamos dentro de seleccionar fecha");

        new DatePickerDialog(getActivity(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();

        if(itemId == R.id.faboptions_seven)
        {

            Intent i = new Intent(getContext(), DatosDiaCalendario.class);

            //Toast.makeText(getContext(), "SEMANAL", 0).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(R.string.elegir).setTitle(R.string.title_f);

            builder.setView(dialogView);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User click OK button
                    lanzarSemanal(fecha, userUid);

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

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(R.string.elegir).setTitle(R.string.title_f);

            builder.setView(R.layout.dialog_fecha);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User click OK button
                    lanzarMensual(fecha, userUid);
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

        }else if(itemId == R.id.faboptions_trimestral)
        {
            Toast.makeText(getContext(), "TRIMESTRAL", 0).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(R.string.elegir).setTitle(R.string.title_f);

            builder.setView(R.layout.dialog_fecha);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User click OK button
                    lanzarTrimestral(fecha, userUid);
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

        }else if(itemId == R.id.faboptions_anual)
        {
            Toast.makeText(getContext(), "ANUAL", 0).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(R.string.elegir).setTitle(R.string.title_f);

            builder.setView(R.layout.dialog_fecha);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User click OK button
                    lanzarAnual(fecha, userUid);
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

        }else
        {
            Toast.makeText(getContext(), "ERROR, NO LLEVA A NINGÚN SITIO", 0).show();
        }

        return super.onOptionsItemSelected(item);
    }

}
