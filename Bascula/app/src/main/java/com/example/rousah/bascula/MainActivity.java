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

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements PerfilFragment.OnFragmentInteractionListener {

    //private SectionsPagerAdapter mSectionsPagerAdapter;

    //--------------Drawer--------------------
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    //--------------Drawer--------------------
    View headerLayout;
    int SELECT_PICTURE_CONSTANT = 0;

    // BORJA
    /*
    Introduced with the purpose to have a database for the remote users
     */
    public static AlmacenUsuariosRemotos almacen = new AlmacenUsuariosRemotosArray();
    FirebaseUser usuario;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    //private ViewPager mViewPager; //Ha causado error?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        */

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
        setTitle("Inicio");
        //--------------Drawer--------------------


        headerLayout = navigationView.getHeaderView(0); // 0-index header
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        mostrarUsuarioNavDrawer(usuario);
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

        if (id == R.id.perfil) {
            lanzarPerfil(null);
            return true;
        }

        if (id == R.id.log_out) {
            FirebaseAuth.getInstance().signOut(); //End user session
            startActivity(new Intent(MainActivity.this, LoginActivity.class)); //Go back to home page
            finish();
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

    public void lanzarPerfil(View view){
        Intent i = new Intent(this, PerfilActivity.class);
        startActivity(i);
    }

    //  BORJA
    /*
     * Function introduced as part of the remote user administration
     *
     * ????????????????????????????????
     * It is not completed yet. some information that shall be passed between activities are not properly set.
     * ????????????????????????????????
     *
     * Its main purpose is to be part of the confirmation whether the administrator actually aims
     * to delete the remote user.
     */
    public void lanzaCheck(View view) {

        TextView fieldTextView = (TextView) findViewById(R.id.usuario);

        Intent intent = new Intent (this, RemoveRemoteCheckActivity.class);

        // Storage of information as data/value into the intent
         intent.putExtra("usuario", fieldTextView.getText().toString());

        // Start activity of communication
        startActivityForResult(intent, 123);   //requestCode shall be between 0=<resultCode =<65535, 1234567 was not accepted

    }


    //  BORJA
    /*
     * Function introduced as part of the remote user administration
     *
     * ????????????????????????????????
     * It is not completed yet. some information shall be managet properly in the layout.
     * ????????????????????????????????
     *
     * Its main purpose is to be part of to receive the acceptace or the rejection of the removal of the remote user
     */
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
    /*
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Tab1 tab1 = new Tab1();
                    return tab1;
                case 1:
                    Tab2 tab2 = new Tab2();
                    return tab2;
                case 2:
                    Tab4 tab4 = new Tab4();
                    return tab4;

                    default:
                        return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.inicio);
                case 1:
                    return getString(R.string.datos);
                case 2:
                    return "Administrar";
                default:
                    return null;
            }
        }
    }*/
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
                        fragmentManager.beginTransaction().replace(R.id.frameContent, selectedFragment).commit();
                        // the current menu item is highlighted in navigation tray.
                        navigationView.setCheckedItem(menuItem.getItemId());
                        setTitle(menuItem.getTitle());
                        //close the drawer when user selects a nav item.
                        drawerLayout.closeDrawers();
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
            case R.id.nav_tercer_fragment:
                //fragment = new TabTercero();
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
        //por si se logea con email y no tiene foto asignada
        else {
            imagenPerfil.setImageDrawable(getDrawable(R.drawable.ic_account_circle_black_55dp));
        }
    }
    //--------------Nav Header----------------


}
