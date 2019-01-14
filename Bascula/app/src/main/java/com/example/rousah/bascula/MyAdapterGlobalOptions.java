package com.example.rousah.bascula;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapterGlobalOptions extends RecyclerView.Adapter<MyAdapterGlobalOptions.ViewHolder> {
    private ArrayList<Double> mDataSet;
    private ArrayList<String> mUnidadesSet;
    private ArrayList<String> mTitleSet;
    private ArrayList<Integer> mImageSet;

    private String TAG = "EQUIPO2/GTI";

    /**
     * El ViewHolder obtiene referencias de los componentes visuales para
     * cada elemento, es decir, referencias de los editText, textView, buttons
     * , etc...
     * Referéncia de los componentes visuales.
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // en este ejemplo cada elemento consta solo de un título
        public CardView cv;
        public TextView t1;
        public TextView t2;
        public ImageView i1;
        public ViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            t1 = itemView.findViewById(R.id.titulo);
            t2 = itemView.findViewById(R.id.valor);
            i1 = itemView.findViewById(R.id.imagen);
        }
    }

    /**
     * Contructor, y metodos para añadir, editar, y eliminar del DataSet
     *
     */
    // Este es nuestro constructor (puede variar según lo que queremos mostrar)
    public MyAdapterGlobalOptions(String tipo, ArrayList<Double> data) {

        mDataSet = new ArrayList<Double>();
        mUnidadesSet = new ArrayList<String>();
        mTitleSet = new ArrayList<String>();
        mImageSet = new ArrayList<Integer>();

        if(tipo == "varios")
        {
            // estructuramos de distinta forma
            // no vamos a tener imagen
        }else
        {
            mImageSet.add(R.drawable.scale_bathroom);
            mTitleSet.add("Peso:");
            mDataSet.add(data.get(0));
            mUnidadesSet.add("Kg");
            mImageSet.add(R.drawable.ruler);
            mTitleSet.add("Altura:");
            mDataSet.add(data.get(1));
            mUnidadesSet.add("m");
            mImageSet.add(R.drawable.ic_scale_balance);
            mTitleSet.add("IMC:");
            mDataSet.add(data.get(2));
            mUnidadesSet.add("Kg/cm^2");
        }


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
    public MyAdapterGlobalOptions.ViewHolder onCreateViewHolder(ViewGroup parent,
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
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