// ROSA, BORJA
/*
VERSION: V 2.0.0
DESCRIPTION:
The graphic part of the remote administration has been introduced.
 */

/*
VERSION: V 1.0.0
DESCRIPTION:
Initial
 */

package com.example.rousah.bascula;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comun.Mqtt;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import static com.example.comun.Mqtt.*;

import static com.example.comun.Mqtt.broker;
import static com.firebase.ui.auth.AuthUI.TAG;


public class MainActivity extends AppCompatActivity implements PerfilFragment.OnFragmentInteractionListener, CasaFragment.OnFragmentInteractionListener, MqttCallback {

    //--------------Drawer--------------------
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    //--------------Drawer--------------------
    View headerLayout;

    public static AlmacenUsuariosRemotos almacen = new AlmacenUsuariosRemotosArray();
    FirebaseUser usuario;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //----------------MQTT---------------------
    MqttClient client;
    //----------------MQTT---------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usuario = FirebaseAuth.getInstance().getCurrentUser();

        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
        fragmentManager.beginTransaction().replace(R.id.frameContent,new TabFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_inicio);
        setTitle("Mediciones");
        //--------------Drawer--------------------


        headerLayout = navigationView.getHeaderView(0); // 0-index header

        mostrarUsuarioNavDrawer(usuario);

        //---------------MQTT---------------------
        try {
            Log.i(Mqtt.TAG, "Conectando al broker " + broker);
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topicRoot+"WillTopic", "App desconectada".getBytes(),
                    qos, false);
            client.connect(connOpts);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al conectar.", e);
        }

        try {
            Log.i(Mqtt.TAG, "Suscrito a " + topicRoot+"cmnd/POWER");
            client.subscribe(topicRoot+"cmnd/POWER", qos);
            client.setCallback(this);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al suscribir.", e);
        }
        //---------------MQTT---------------------
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
                            Log.w("Fragment seleccionado", selectedFragment.toString());
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
     * android:configChanges="keyboardHidden|orientation|screenSize" in your activity tag of the manifest for this to work.
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
    void mostrarUsuarioNavDrawer(FirebaseUser usuario) {
        TextView nombre = headerLayout.findViewById(R.id.nombreNav);
        nombre.setText(usuario.getDisplayName());

        TextView email = headerLayout.findViewById(R.id.emailNav);
        email.setText(usuario.getEmail());

        final ImageView imagenPerfil = headerLayout.findViewById(R.id.imagenNav);
        String proveedor = usuario.getProviders().get(0);
        //checkea si el proveedor es de google por si se logea con un email
        if(proveedor.equals("google.com")) {
            String uri = usuario.getPhotoUrl().toString();
            //carga la foto y usa transform para hacerla circular
            Picasso.with(getBaseContext()).load(uri).transform(new CircleTransform()).into(imagenPerfil);
            System.out.println("dentro de getPhoto");
        }
        else {
            Picasso.with(this).load(R.drawable.round_account_circle_black_48dp).transform(new CircleTransform()).into(imagenPerfil);
        }
    }
    //--------------Nav Header----------------




    //---------------MQTT------------------------
    public void botonLuces (View view) {
        try {
            Log.i(Mqtt.TAG, "Publicando mensaje: " + "toggle sonoff");
            MqttMessage message = new MqttMessage("TOGGLE".getBytes());
            message.setQos(qos);
            message.setRetained(false);
            client.publish(topicRoot+"cmnd/POWER", message);
        } catch (MqttException e) {
            Log.e(TAG, "Error al publicar.", e);
        }
        Snackbar.make(view, "Publicando en MQTT by rous", Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    @Override public void onDestroy() {
        try {
            Log.i(TAG, "Desconectado");
            client.disconnect();
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar.", e);
        }
        super.onDestroy();
    }

    @Override
    public void connectionLost(Throwable cause) {
        while (!isNetworkAvailable()) {
            Log.d(TAG, "Reintentando conexión MQTT");
            try {
                Log.i(Mqtt.TAG, "Conectando al broker " + broker);
                client = new MqttClient(broker, clientId, new MemoryPersistence());
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setKeepAliveInterval(60);
                connOpts.setWill(topicRoot+"WillTopic", "App desconectada".getBytes(),
                        qos, false);
                client.connect(connOpts);
            } catch (MqttException e) {
                Log.e(Mqtt.TAG, "Error al conectar.", e);
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        final String payload = new String(message.getPayload());
        Log.d(Mqtt.TAG, "Recibiendo: " + topic + "->" + payload);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Switch luces = findViewById(R.id.switchluces);
                if (luces != null) {
                    if (payload.contains("ON")) {
                        luces.setChecked(true);
                        Toast.makeText(getApplicationContext(), "Luces encendidas", Toast.LENGTH_SHORT).show();
                        Log.d(Mqtt.TAG, "encendiendo");
                    }
                    if (payload.contains("OFF")) {
                        luces.setChecked(false);
                        Toast.makeText(getBaseContext(), "Luces apagadas", Toast.LENGTH_SHORT).show();
                        Log.d(Mqtt.TAG, "apagando");
                    }
                }
            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "Entrega completa");
    }
    //---------------MQTT------------------------




    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d("internet", activeNetworkInfo.toString());
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
