package com.example.rousah.bascula;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comun.Mqtt;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import static com.example.comun.Mqtt.broker;
import static com.example.comun.Mqtt.clientId;
import static com.example.comun.Mqtt.qos;
import static com.example.comun.Mqtt.topicRoot;
import static com.firebase.ui.auth.AuthUI.TAG;



public class MainActivity extends AppCompatActivity implements PerfilFragment.OnFragmentInteractionListener, CasaFragment.OnFragmentInteractionListener, TratamientosFragment.OnFragmentInteractionListener, HospitalesFragment.OnFragmentInteractionListener, MqttCallback {

    //--------------Drawer--------------------
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    //--------------Drawer--------------------
    View headerLayout;

    NotificationManager manager;
    Notification myNotication;

    public static AlmacenUsuariosRemotos almacen = new AlmacenUsuariosRemotosArray();

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    //----------------MQTT---------------------
    MqttClient client;
    //----------------MQTT---------------------


    // caídas
    private static final int SOLICITUD_PERMISO_GLOBAL = 0;
    private String[] permisos = {Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }


        });
        //--------------Drawer--------------------
        initializeStuff();

        // since, NoActionBar was defined in theme, we set toolbar as our action bar.
        setSupportActionBar(toolbar);

        //this basically defines on click on each menu item.
        setUpNavigationView(navigationView);

        //This is for the Hamburger icon.
        drawerToggle = setupDrawerToggle();
        drawerLayout.addDrawerListener(drawerToggle);

        //Inflate the first fragment,this is like home fragment before user selects anything.
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameContent, new TabFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_inicio);
        setTitle("Mediciones");
        //--------------Drawer--------------------


        headerLayout = navigationView.getHeaderView(0); // 0-index header
        mostrarUsuarioNavDrawer();

        //---------------MQTT---------------------
        try {
            Log.i(Mqtt.TAG, "Conectando al broker " + Mqtt.broker);
            client = new MqttClient(Mqtt.broker, Mqtt.clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(Mqtt.topicRoot+"WillTopic", "App desconectada".getBytes(),
                    Mqtt.qos, false);
            client.connect(connOpts);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al conectar.", e);
        }

        try {
            Log.i(Mqtt.TAG, "Suscrito a " + Mqtt.topicRoot+"POWER");
            client.subscribe(Mqtt.topicRoot+"POWER", Mqtt.qos);
            client.setCallback(this);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al suscribir.", e);
        }

        try {
            Log.i(Mqtt.TAG, "Suscrito a " + Mqtt.topicRoot+"PRESENCIA");
            client.subscribe(Mqtt.topicRoot+"PRESENCIA", Mqtt.qos);
            client.setCallback(this);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al suscribir.", e);
        }
        //---------------MQTT---------------------


        //---------------PERMISOS-------------------
        /**
         * Caídas y mapa
         */


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            PedirPermisos.solicitarPermiso(permisos, "Sin el permiso llamar no puedo llamar a emergencias si se detecta alguna caída.",
                    SOLICITUD_PERMISO_GLOBAL, this);
            return;
        }
        else {
            SharedPreferences pref =
                    PreferenceManager.getDefaultSharedPreferences(this);
            if(pref.getString("llamadaEmergencia","?").equals("1")){
            }else {
                crearServicio();
            }
        }

        //mostrarPreferencias();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * This makes sure that the action bar home button that is the toggle button, opens or closes the drawer when tapped.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            lanzarPreferencias(null);
            return true;
        }
        if (id == R.id.acercaDe) {
            lanzarAcercaDe(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void lanzarAcercaDe(View view){
        Intent i = new Intent(this, AcercaDeActivity.class);
        startActivity(i);
    }

    public void lanzarPreferencias(View view){
        Intent i = new Intent(this, PreferenciasActivity.class);
        startActivity(i);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);  // recommended by www.youtube.com/watch?v=OHyPQ4tpBuc
        if (requestCode == 123 && resultCode == RESULT_OK) {
            String resultado = (String) data.getExtras().getString("resultado");

            // View of interest in the activity_main layout
            // TextView fieldTextView = (TextView) findViewById(R.id.textView);
            // fieldTextView.setText("Resultado: " + resultado);
        }

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String usuario = (String) data.getExtras().getString("usuarioremoto");
            // String email = (String) data.getExtras().getString("email");

            Intent intent = new Intent(this, RemoveRemoteCheckActivity.class);
            intent.putExtra("usuarioremoto", usuario);
            // startActivity(intent);
            startActivityForResult(intent, 2);
            finish();
        }

    }



    //  BORJA
    /*
     * Function introduced as part of the remote user administration. It will trigger the
     * Removal of remote users
     */
    public void lanzarEliminacionUsuariosRemotos(View view) {
        Intent intent = new Intent (this, UsuariosRemotosActivity.class);

        // Start activity of communication
        startActivity(intent);
    }

    //  BORJA
    /*
     * Function introduced as part of the remote user administration. It will trigger the
     * Removal of remote users
     */
    public void lanzarRegistroUsuariosRemotos(View view) {

        Intent intent_b = new Intent (this, RegistroUsuarioRemotoActivity.class);

        // Start activity of communication
        startActivity(intent_b);   //requestCode shall be between 0=<resultCode =<65535, 1234567 was not accepted

    }


    //--------------Drawer--------------------
    void initializeStuff(){
        drawerLayout =(DrawerLayout) findViewById(R.id.drawerLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigationView = (NavigationView) findViewById(R.id.navigationDrawer);
    }

    /**
     * Inflate the fragment according to item clicked in navigation drawer.
     */
    private void setUpNavigationView(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        //replace the current fragment with the new fragment.
                        Fragment selectedFragment = selectDrawerItem(menuItem);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        if (selectedFragment == null ) {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class)); //Go back to home page
                        }
                        else {
                            fragmentManager.beginTransaction().replace(R.id.frameContent, selectedFragment).commit();
                            // the current menu item is highlighted in navigation tray.
                            navigationView.setCheckedItem(menuItem.getItemId());
                            setTitle(menuItem.getTitle());
                            //close the drawer when user selects a nav item.
                            drawerLayout.closeDrawers();
                        }
                        return true;
                    }
                });
    }

    /**
     * This method returns the fragment according to navigation item selected.
     */
    public Fragment selectDrawerItem(MenuItem menuItem){
        Fragment fragment = null;
        switch(menuItem.getItemId()) {
            case R.id.nav_inicio:
                fragment = new TabFragment();
                break;
            case R.id.nav_perfil:
                fragment = new PerfilFragment();
                break;
            case R.id.nav_casa:
                fragment = new CasaFragment();
                break;
            case R.id.nav_tratamientos:
                fragment = new TratamientosFragment();
                break;
            case R.id.nav_hospitales:
                fragment = new HospitalesFragment();
                break;
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut(); //End user session
                startActivity(new Intent(MainActivity.this, LoginActivity.class)); //Go back to home page
                MainActivity.this.finish();
               // fragment = new TabFragment();
                break;
        }
        return fragment;
    }

    /**
     * This is to setup our Toggle icon. The strings R.string.drawer_open and R.string.drawer close, are for accessibility (generally audio for visually impaired)
     * use only. It is now showed on the screen. While the remaining parameters are required initialize the toggle.
     */
    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar,R.string.drawer_open,R.string.drawer_close);
    }



    /**
     * This synchronizes the drawer icon that rotates when the drawer is swiped left or right.
     * Called inside onPostCreate so that it can synchronize the animation again when the Activity is restored.
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    /**
     * This is to handle generally orientation changes of your device. It is mandatory to include
     * android:configChanges="keyboardHidden|orientation|screenSize" in your activity Mqtt.TAG of the manifest for this to work.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //do something here
    }
    //--------------Drawer--------------------



    //--------------Nav Header----------------
    void mostrarUsuarioNavDrawer() {
        final FirebaseUser usuario;
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        TextView nombre = headerLayout.findViewById(R.id.nombreNav);
        nombre.setText(usuario.getDisplayName());

        TextView email = headerLayout.findViewById(R.id.emailNav);
        email.setText(usuario.getEmail());

        comprobarImagen();
        //por si se logea con email y no tiene foto asignada

                /*if(proveedor.equals("google.com") && storageReference.child("usuarios/" + uid + "/imagenUsuario.jpg") == null) {
            String uri = usuario.getPhotoUrl().toString();
            //carga la foto y usa transform para hacerla circular
            Picasso.with(getBaseContext()).load(uri).transform(new CircleTransform()).into(imagenPerfil);
            System.out.println("dentro de getPhoto");
            Toast.makeText(getBaseContext(), "Googleado", Toast.LENGTH_LONG).show();

        } else if (storageReference.child("usuarios/" + uid + "/imagenUsuario.jpg") != null) {
            storageReference.child("usuarios/" + uid + "/imagenUsuario.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    //Toast.makeText(CrearPerfil.this, uri.toString(), Toast.LENGTH_LONG).show();
                    Picasso.with(getBaseContext()).load(uri.toString()).resize(168, 168).centerCrop()
                            .transform(new CircleTransform())
                            .into(imagenPerfil);
                    System.out.println("dentro de getPhoto");
                }
            });
        }
        //por si se logea con email y no tiene foto asignada
        else {
            //   imagenPerfil.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_account_circle_black_55dp, null));
            Picasso.with(getBaseContext()).load(R.drawable.round_account_circle_black_48dp).transform(new CircleTransform()).into(imagenPerfil);
        }*/




        /*String proveedor = usuario.getProviders().get(0);
        //checkea si el proveedor es de google por si se logea con un email
        if(proveedor.equals("google.com")) {
            String uri = usuario.getPhotoUrl().toString();
            //carga la foto y usa transform para hacerla circular
            Picasso.with(getBaseContext()).load(uri).transform(new CircleTransform()).into(imagenPerfil);
            System.out.println("dentro de getPhoto");
        }
        //por si se logea con email y no tiene foto asignada
        else {
            imagenPerfil.setImageDrawable(getDrawable(R.drawable.ic_account_circle_black_55dp));
        }*/
    //--------------Nav Header----------------
    }

    public void comprobarImagen(){
        final FirebaseUser usuario;
        usuario = FirebaseAuth.getInstance().getCurrentUser();

        //variables: imagen en Storage, uid del user actual y el proveedor de google
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String proveedor = usuario.getProviders().get(0);

        //imagenPerfil = headerLayout.findViewById(R.id.imagenNav);
        final ImageView imagenPerfil = headerLayout.findViewById(R.id.imagenNav);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        storageReference.child("imagenesPerfil/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getBaseContext()).load(uri.toString()).fit().centerCrop()
                        .transform(new CircleTransform())
                        .into(imagenPerfil);
                System.out.println("dentro de getPhoto");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (proveedor.equals("google.com")) {
                    final String uri = usuario.getPhotoUrl().toString();
                    //carga la foto y usa transform para hacerla circular
                    Picasso.with(getBaseContext()).load(uri).resize(168, 168).transform(new CircleTransform()).into(imagenPerfil);
                    System.out.println("dentro de getPhoto");
                } else {
                    //   imagenPerfil.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_account_circle_black_55dp, null));
                    Picasso.with(getBaseContext()).load(R.drawable.round_account_circle_black_48dp).resize(168, 168).transform(new CircleTransform()).into(imagenPerfil);

                }
            }
        });
    }


    //---------------MQTT------------------------
    public void botonLuces (View view) {
        try {
            Log.i(Mqtt.TAG, "Publicando mensaje: " + "toggle sonoff");
            MqttMessage message = new MqttMessage("TOGGLE".getBytes());
            message.setQos(Mqtt.qos);
            message.setRetained(false);
            client.publish(Mqtt.topicRoot+"cmnd/POWER", message);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al publicar.", e);
        }
        Snackbar.make(view, "Publicando en MQTT by rous", Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    @Override public void onDestroy() {
        try {
            Log.i(Mqtt.TAG, "Desconectado");
            client.disconnect();
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al desconectar.", e);
        }
        super.onDestroy();
    }

    @Override
    public void connectionLost(Throwable cause) {
        while (!isNetworkAvailable()) {
            Log.d("MQTT", "Reintentando conexión MQTT");
            try {
                Log.i(Mqtt.TAG, "Conectando al broker " + broker);
                client = new MqttClient(Mqtt.broker, Mqtt.clientId, new MemoryPersistence());
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setKeepAliveInterval(60);
                connOpts.setWill(Mqtt.topicRoot+"WillTopic", "App desconectada".getBytes(),
                        Mqtt.qos, false);
                client.connect(connOpts);
            } catch (MqttException e) {
                Log.e(Mqtt.TAG, "Error al conectar.", e);
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        final String payload = new String(message.getPayload());
        Log.d("MQTT", "Recibiendo: " + topic + "->" + payload);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("RUNAPP","LOS MENSAJES SE ESTAN RUNEANDO");
                Switch luces = findViewById(R.id.switchluces);
                if (luces != null) {
                    if (payload.contains("ON")) {
                        luces.setChecked(true);
                        Toast.makeText(getBaseContext(), getResources().getString(R.string.lucesEncendidas), Toast.LENGTH_SHORT).show();
                    }
                    if (payload.contains("OFF")) {
                        luces.setChecked(false);
                        Toast.makeText(getBaseContext(), getResources().getString(R.string.lucesApagadas), Toast.LENGTH_SHORT).show();
                    }
                }

                if(payload.contains("IN")){
                    notificacionDentro();
                }
                if(payload.contains("OUT")){
                    notificacionFuera();
                }



            }

            private void notificacionFuera() {
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("default",
                            "NOMBRE_DEL_CANAL",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("DESCRIPCION_DEL_CANAL");
                    mNotificationManager.createNotificationChannel(channel);
                }



                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                        .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                        .setContentTitle(getResources().getString(R.string.hastaP)) // title for notification
                        .setContentText(getResources().getString(R.string.haveAneatDay))// message for notification
                        .setAutoCancel(true); // clear notification after click
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                mNotificationManager.notify(0, mBuilder.build());
            }



            private void notificacionDentro() {
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("default",
                            "NOMBRE_DEL_CANAL",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("DESCRIPCION_DEL_CANAL");
                    mNotificationManager.createNotificationChannel(channel);
                }



                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                        .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                        .setContentTitle(getResources().getString(R.string.bienvenido)) // title for notification
                        .setContentText(getResources().getString(R.string.bienvenidoCasa))// message for notification
                        .setAutoCancel(true); // clear notification after click
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                mNotificationManager.notify(0, mBuilder.build());
            }

        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(Mqtt.TAG, "Entrega completa");
    }
    //---------------MQTT------------------------


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d("internet", activeNetworkInfo.toString());
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    //---------------CAÍDAS-------------------
    public void crearServicio() {
        Intent i = new Intent(this, ServicioCaidas.class);
        startService(i);
    }

    /*@Override public void onRequestPermissionsResult(int requestCode,
                                                     String[] permissions, int[] grantResults) {
        if (requestCode == SOLICITUD_PERMISO_CALL_PHONE) {
            if (grantResults.length== 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                crearServicio();
            }
            else {
                Toast.makeText(this, getResources().getString(R.string.permisoCaida), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == SOLICITUD_PERMISO_ACCESS_FINE_LOCATION) {
            if (permissions.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
            else {
                Toast.makeText(this, getResources().getString(R.string.permisoMapa), Toast.LENGTH_SHORT).show();
            }
        }
    }*/
    //---------------CAÍDAS-------------------


}
