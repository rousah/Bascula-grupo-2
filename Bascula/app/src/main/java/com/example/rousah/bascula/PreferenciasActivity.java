package com.example.rousah.bascula;

import android.app.Activity;
import android.os.Bundle;

public class PreferenciasActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferenciasFragment())
                .commit();
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}