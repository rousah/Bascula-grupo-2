package com.example.rousah.bascula;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
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
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.util.Tools;
import com.db.chart.view.LineChartView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    private OnFragmentInteractionListener mListener;

    int i = 0;

    private LineChartView grafica;

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
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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




        final String[] labels = new String[3];
        final float[] values = new float[3];
        final Runnable mBaseAction;
        final Tooltip mTip = new Tooltip(getContext(), R.layout.tooltip, R.id.value);

        db.collection("usuarios")
                .document(usuario.getUid())
                .collection("mediciones")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(3)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                 @Override
                                                 public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                     if (task.isSuccessful()) {
                                                         int i = 0;
                                                         for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                             float peso = Float.parseFloat(documentSnapshot.getData().get("peso").toString());
                                                             String fecha = documentSnapshot.getData().get("fecha").toString();
                                                             fecha = fecha.substring(4, 10);
                                                             labels[i] = fecha;
                                                             values[i] = peso;
                                                             i++;
                                                         }
                                                         ((TextView) mTip.findViewById(R.id.value)).setTypeface(
                                                                 Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Semibold.ttf"));

                                                         mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
                                                         mTip.setDimensions((int) Tools.fromDpToPx(58), (int) Tools.fromDpToPx(25));
                                                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

                                                             mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                                                                     PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                                                                     PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

                                                             mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                                                                     PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                                                                     PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

                                                             mTip.setPivotX(Tools.fromDpToPx(65) / 2);
                                                             mTip.setPivotY(Tools.fromDpToPx(25));
                                                         }

                                                         LineSet dataset = new LineSet(labels, values);

                                                         dataset.setColor(Color.parseColor("#399699"))
                                                                 .setDotsColor(Color.parseColor("#ff869b"))
                                                                 .setThickness(4)
                                                                 .setDashed(new float[]{10f, 10f});

                                                        /* mBaseAction = action;
                                                         Runnable chartAction = new Runnable() {
                                                             @Override
                                                             public void run() {

                                                                 mBaseAction.run();
                                                                 mTip.prepare(mChart.getEntriesArea(0).get(3), mValues[0][3]);
                                                                 mChart.showTooltip(mTip, true);
                                                             }
                                                         };

                                                         */

                                                         Paint paint = new Paint();
                                                         paint.setColor(Color.parseColor("#E3E3E3"));
                                                         DecimalFormat formato = new DecimalFormat();
                                                         formato.applyPattern("#0.0");
                                                         grafica = view.findViewById(R.id.linechart);
                                                         grafica.addData(dataset);
                                                         grafica.setAxisColor(Color.parseColor("#399699"))
                                                                 .setYAxis(false)
                                                                 .setYLabels(AxisRenderer.LabelPosition.NONE)
                                                                 .setGrid(10, 20, paint)
                                                                 .setAxisBorderValues(0, 100)
                                                                 .setTypeface(Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Semibold.ttf"))
                                                                 .setLabelsFormat(formato)
                                                                 .setTooltips(mTip)
                                                                 .show(new Animation().setInterpolator(new BounceInterpolator())
                                                                         .fromAlpha(0));

                                                         grafica.show();
                                                     }
                                                 }
                                             });


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
