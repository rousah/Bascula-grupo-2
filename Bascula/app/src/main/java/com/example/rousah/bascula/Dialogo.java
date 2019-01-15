package com.example.rousah.bascula;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Dialogo extends DialogFragment {
    String[] tvshows={"Crisis","Blindspot","BlackList","Game of Thrones","Gotham","Banshee"};
    RecyclerView rv;
    MyAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
            View view = inflater.inflate(R.layout.datos_un_dia_cal, container);
            rv = view.findViewById(R.id.recycler_view_measurement);
            rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));

            //adapter = new MyAdapter(this.getActivity(), tvshows);

            //rv.setAdapter(adapter);

            this.getDialog().setTitle("TV Shows");
            return view;
    }
}
