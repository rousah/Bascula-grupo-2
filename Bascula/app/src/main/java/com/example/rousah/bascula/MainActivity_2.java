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
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity_2 extends AppCompatActivity {

    // BORJA
    /*
    Introduced with the purpose to have a database for the remote users
     */
    public static AlmacenUsuariosRemotos almacen = new AlmacenUsuariosRemotosArray();


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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










    /**
     * A placeholder fragment containing a simple view.
     */



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
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
                    Tab3 tab3 = new Tab3();
                    return tab3;
                case 3:
                    Tab4 tab4 = new Tab4();
                    return tab4;

                    default:
                        return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.inicio);
                case 1:
                    return getString(R.string.datos);
                case 2:
                    return getString(R.string.preferencias);

                case 3:
                    return "cuarto";

                default:
                    return null;
            }
        }
    }

}
