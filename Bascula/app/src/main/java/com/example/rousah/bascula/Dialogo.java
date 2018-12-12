package com.example.rousah.bascula;

import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Dialogo extends DialogFragment {

    public Dialogo ()
    {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
            return crearDialogoSimple();
    }

    public AlertDialog crearDialogoSimple()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = getActivity().getLayoutInflater().inflate(R.layout.datos_un_dia_cal, null);

        builder.setView(v);

        return builder.create();
    }

}
