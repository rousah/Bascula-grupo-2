package com.example.rousah.bascula;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.LineChartView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

import static android.view.KeyCharacterMap.FULL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CasaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CasaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CasaFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private LineChartView grafica;
    private Runnable mBaseAction;

    public CasaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CasaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CasaFragment newInstance(String param1, String param2) {
        CasaFragment fragment = new CasaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.casa, container, false);

        db.collection("usuarios").document(usuario.getUid()).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task){
                        if (task.isSuccessful()) {

                            TextView temp = view.findViewById(R.id.temp);
                            temp.setText(task.getResult().getDouble("temperatura").toString() + " ºC");

                            TextView hum = view.findViewById(R.id.hum);
                            hum.setText(task.getResult().getDouble("temperatura").toString() + " %");

                            TextView termi = view.findViewById(R.id.termi);
                            termi.setText(task.getResult().getDouble("sensaciontermica").toString() + " ºC");
                        } else {
                            Log.e("Firestore", "Error al leer", task.getException());
                        }
                    }
                });

        String[] labels = new String[2];
        labels[0] = "12:00";
        labels[1] = "13:00";
        float[] values = new float[2];
        values[0] = 20.0f;
        values[1] = 30.0f;

        LineSet dataset = new LineSet(labels, values);
        dataset.addPoint(new Point("14:00", 40.0f));

        dataset.setColor(Color.parseColor("#399699"))
                .setDotsColor(Color.parseColor("#ff869b"))
                .setThickness(4)
                .setDashed(new float[]{10f, 10f});

        DecimalFormat formato = new DecimalFormat();
        formato.applyPattern("#0.00");

        grafica = view.findViewById(R.id.linechart);
        grafica.addData(dataset);
        grafica.setAxisColor(Color.parseColor("#399699"))
                .setTypeface(Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Semibold.ttf"))
                .setLabelsFormat(formato);

        grafica.show();


        // Inflate the layout for this fragment
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
