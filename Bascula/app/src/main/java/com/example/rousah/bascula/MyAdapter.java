package com.example.rousah.bascula;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<Double> mDataSet;
    private ArrayList<String> mUnidadesSet;
    private ArrayList<String> mTitleSet;
    private ArrayList<Integer> mImageSet;

    private String TAG = "EQUIPO2/GTI";

    /**
     * El ViewHolder obtiene referencias de los componentes visuales para
     * cada elemento, es decir, referencias de los edtiText, textView, buttons
     * , etc...
     * Referencia de los componentes visuales.
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // en este ejemplo cada elemento consta solo de un título
        public CardView cv;
        public TextView t1;
        public TextView t2;
        public ImageView i1;
        public ViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            t1 = (TextView)itemView.findViewById(R.id.titulo);
            t2 = (TextView)itemView.findViewById(R.id.valor);
            i1 = (ImageView)itemView.findViewById(R.id.imagen);
        }
    }

    /**
     * Contructor, y metodos para añadir, editar, y eliminar del DataSet
     *
     */
    // Este es nuestro constructor (puede variar según lo que queremos mostrar)
    public MyAdapter(ArrayList<Double> data, Context context) {

        mDataSet = new ArrayList<Double>();
        mUnidadesSet = new ArrayList<String>();
        mTitleSet = new ArrayList<String>();
        mImageSet = new ArrayList<Integer>();

        mImageSet.add(R.drawable.scale_bathroom);

        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);

        mTitleSet.add("Peso:");
        if(pref.getString("peso","?").equals("1")){
            mDataSet.add(data.get(0));
            mUnidadesSet.add("kg");
        }else {
            /*
            Double n = data.get(0)*2.2;
            String s = n.toString();
            s = String.format("%.2f", s);
            */
            Double x = 2.2;
            Double peso = data.get(0) * x;
            String y = String.valueOf(peso);
            y = String.format("%.2f", peso);
            Double z = Double.parseDouble(y);
            mDataSet.add(z);
            mUnidadesSet.add("libras");
        }

        mTitleSet.add("Altura:");
        if(pref.getString("altura","?").equals("1")) {
            mDataSet.add(data.get(1));
            mUnidadesSet.add("m");
        }else{
            Double x = 3.2;
            Double altura = data.get(1) * x;
            String y = String.valueOf(altura);
            y = String.format("%.2f", altura);
            Double z = Double.parseDouble(y);
            mDataSet.add(z);
            mUnidadesSet.add("pies");
        }

        mImageSet.add(R.drawable.ruler);
        mImageSet.add(R.drawable.ic_scale_balance);
        mTitleSet.add("IMC:");
        mDataSet.add(data.get(2));
        mUnidadesSet.add("Kg/cm^2");

    }

    public void setDataSet(ArrayList<Double> DataSet) {
        mDataSet = DataSet;
        notifyDataSetChanged();
    }

    /**
     * El layout manager invoca este método para renderizar
     * cada elemento del RecyclerView, inflando el layout archivo.xml
     * (layout que le indicamos) que representa a nuestros
     * elementos y devuelve una instancia de la clase ViewHolder
     * que antes definimos.
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // Creamos una nueva vista
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dato_dia_cal, parent, false);


        // Aquí podemos definir tamaños, márgenes, paddings
        // ...

        ViewHolder vh = new ViewHolder(cv);
        return vh;
    }
    @Override public int getItemViewType(int position) {
        return position;
    }
    /**
     * Este método reemplaza el contenido de cada view, para cada
     * elemento de la lista (nótese el argumento position)
     *
     * @param holder
     * @param position
     */

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - obtenemos un elemento del dataset según su posición
        // - reemplazamos el contenido de los views según tales datos
        holder.t1.setText((CharSequence) mTitleSet.get(position));
        holder.t2.setText(String.valueOf(mDataSet.get(position))+" "+mUnidadesSet.get(position));
        holder.i1.setImageResource(mImageSet.get(position));

    }

    /**
     * Método que define la cantidad de elementos del RecyclerView.
     * Puede ser más complejo (por ejemplo: si implementamos filtros o búsquedas)
     * @return
     */
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}

