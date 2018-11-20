package com.example.intentservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

public class IntentServiceOperacion extends IntentService {
    public IntentServiceOperacion() {
        super("IntentServiceOperacion");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        double n = intent.getExtras().getDouble("numero");
        SystemClock.sleep(5000);
        MainActivity.salida.append(n*n + "\n");
    }
}