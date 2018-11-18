package com.equipodos.raspberry;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ArduinoUart {

    private UartDevice uart;

        public ArduinoUart(String nombre, int baudios) {
            try {
                uart = PeripheralManager.getInstance().openUartDevice(nombre);
                uart.setBaudrate(baudios);
                uart.setDataSize(8);
                uart.setParity(UartDevice.PARITY_NONE);
                uart.setStopBits(1);
            } catch (IOException e) {
                Log.w(TAG, "Error iniciando UART", e);
            }
        }
        public void escribir(String s) {
            try {
                int escritos = uart.write(s.getBytes(), s.length());
                Log.d(TAG, escritos+" bytes escritos en UART");
            } catch (IOException e) {
                Log.w(TAG, "Error al escribir en UART", e);
            }
        }


    public String leer() {
        String s = "";
        int len;
        final int maxCount = 8; // Máximo de datos leídos cada vez
        byte[] buffer = new byte[maxCount];
        try {
            do {
                len = uart.read(buffer, buffer.length);
                for (int i=0; i<len; i++) {
                    s += (char)buffer[i];
                }
            } while(len>0);
        } catch (IOException e) {
            Log.w(TAG, "Error al leer de UART", e);
        }
        return s;
    }
    public void cerrar() {
        if (uart != null) {
            try {
                uart.close();
                uart = null;
            } catch (IOException e) {
                Log.w(TAG, "Error cerrando UART", e);
            }
        }
    }
    static public List<String> disponibles() {
        return PeripheralManager.getInstance().getUartDeviceList();
    }

}