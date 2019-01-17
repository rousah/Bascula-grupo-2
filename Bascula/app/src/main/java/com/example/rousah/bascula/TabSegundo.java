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

public class TabSegundo extends Fragment implements View.OnClickListener {
    private View view;
    private String TAG = "MATTHEW/GTI";
    private String fecha;
    private String userUid;
    private Button ok;
    private FirebaseUser usuario;
    private Calendar myCalendar;
    private Button fLabel;
    private LayoutInflater inflater;
    private View dialogView;
    Fragment esto;
    AlertDialog.Builder builder3;
    AlertDialog.Builder builder2;
    Dialog dialogo2;


    public static AlertDialog.Builder builderAnual;
    public static AlertDialog dialogAnual;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        userUid = usuario.getUid();
        setHasOptionsMenu(true);
        esto = this;


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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_segundo, null, false);

        FabOptions fabOptions = view.findViewById(R.id.fab_options);
        fabOptions.setButtonsMenu(R.menu.menu_filter);

        fabOptions.setBackgroundColor(getContext(), getResources().getColor(R.color.colorPrimaryDark));
        fabOptions.setFabColor(R.color.colorAccent);

        fabOptions.setButtonColor(R.id.faboptions_seven, R.color.white);
        fabOptions.setButtonColor(R.id.faboptions_mes, R.color.white);
        fabOptions.setButtonColor(R.id.faboptions_trimestral, R.color.white);
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

        public void lanzarGrafica (String f, String user, int numero)
        {
            Intent s = new Intent(getContext(), Grafica.class);
            s.putExtra("fecha", f);
            s.putExtra("numDatos", numero);
            startActivity(s);
        }


        /**
         * Funciones para calendario seleccionar último día del que quieres ver datos
         *
         * updateLabel()
         * seleccionaFecha()
         */
        private void updateLabel () {

            Log.d(TAG, "Dentro de update label");
            String myFormat = "dd/MM/yy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            Log.d(TAG, sdf.format(myCalendar.getTime()));
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
                Log.d(TAG, "Fecha: " + dayOfMonth + "-" + String.valueOf(month).toString() + "-" + year);

                String myFormat = "dd/MM/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                fecha = sdf.format(myCalendar.getTime());

                updateLabel();
            }

        };

        public void seleccionaFecha (View view){

            Log.d(TAG, "Estamos dentro de seleccionar fecha");

            new DatePickerDialog(getActivity(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }


        @Override
        public void onClick (View v){

            switch (v.getId()) {
                case R.id.faboptions_anual:
                    builder3 = new AlertDialog.Builder(getContext());
                    final Dialog dialogo3;

                    builder3.setMessage(R.string.elegir).setTitle(R.string.title_f);
                    builder3.setView(dialogView);
                    builder3.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // User click OK button
                            lanzarGrafica(fecha, userUid, 365);
                            Toast.makeText(getContext(), "Anual desde " + fecha, Toast.LENGTH_SHORT);
                            onDestroyDialog();
                        }
                    });

                    builder3.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onDestroyDialog();
                        }
                    });

                    dialogo3 = builder3.create();

                    dialogo3.show();

                    break;
                case R.id.faboptions_seven:
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    final Dialog dialogo;

                    builder.setMessage(R.string.elegir).setTitle(R.string.title_f);

                    builder.setView(dialogView);

                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // User click OK button
                            lanzarGrafica(fecha, userUid, 7);
                            Toast.makeText(getContext(), "Semanal desde " + fecha, Toast.LENGTH_SHORT);
                            onDestroyDialog();
                        }
                    });

                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onDestroyDialog();
                        }
                    });

                    dialogo = builder.create();

                    dialogo.show();

                    break;
                case R.id.faboptions_mes:
                    Toast.makeText(getContext(), "MENSUAL", Toast.LENGTH_SHORT).show();
                    final AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                    final Dialog dialogo2;

                    builder2.setMessage(R.string.elegir).setTitle(R.string.title_f);

                    builder2.setView(dialogView);

                    builder2.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // User click OK button
                            lanzarGrafica(fecha, userUid, 30);
                            Toast.makeText(getContext(), "Mensual desde " + fecha, Toast.LENGTH_SHORT);
                            onDestroyDialog();
                        }
                    });

                    builder2.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onDestroyDialog();
                        }
                    });

                    dialogo2 = builder2.create();

                    dialogo2.show();
                    break;
                case R.id.faboptions_trimestral:
                    final AlertDialog.Builder builder4 = new AlertDialog.Builder(getActivity());
                    final Dialog dialogo4;

                    builder4.setMessage(R.string.elegir).setTitle(R.string.title_f);

                    builder4.setView(dialogView);

                    builder4.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // User click OK button
                            lanzarGrafica(fecha, userUid, 90);
                            onDestroyDialog();
                            Toast.makeText(getContext(), "Trimestral desde " + fecha, Toast.LENGTH_SHORT);
                        }
                    });

                    builder4.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onDestroyDialog();
                        }
                    });

                    dialogo4 = builder4.create();
                    dialogo4.show();

                    break;
                default:
            }

        }
    public void onDestroyDialog() {
        super.onDestroy();
        if (dialogView != null) {
            ViewGroup parent = (ViewGroup) dialogView.getParent();
            if (parent != null) {
                parent.removeAllViews();
            }
        }
    }

}
