package com.example.rousah.bascula;

import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.util.Tools;
import com.db.chart.view.LineChartView;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;

public class Grafica extends AppCompatActivity {
    LineChartView grafica;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    Activity esto;
    Bundle bundle;
    int num;
    String fecha;
    private Button cerrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        esto = this;

        bundle = getIntent().getExtras();

        num = bundle.getInt("numDatos");
        fecha = bundle.getString("fecha");

        setContentView(R.layout.activity_grafica);

        cerrar = findViewById(R.id.cerrarGrafica);

        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Runnable mBaseAction;
        final Tooltip mTip = new Tooltip(this, R.layout.tooltip, R.id.value);


        db.collection("usuarios")
                .document(usuario.getUid())
                .collection("mediciones")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .startAt(fecha)
                .limit(num)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = task.getResult().size()-1;
                            final String[] labels = new String[task.getResult().size()];
                            final float[] values = new float[task.getResult().size()];

                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        float peso = Float.parseFloat(documentSnapshot.getData().get("peso").toString());
                        String fecha = documentSnapshot.getData().get("fecha").toString();
                        fecha = fecha.substring(4, 10);
                        labels[i] = fecha;
                        values[i] = peso;
                        i--;
                    }

                    ((TextView) mTip.findViewById(R.id.value)).setTypeface(
                            Typeface.createFromAsset(esto.getAssets(), "OpenSans-Semibold.ttf"));

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
                            .setDashed(new float[]{10f, 10f})
                    //  .setFill(Color.parseColor("#3d6c73"))
                    //  .setGradientFill(new int[]{Color.parseColor("#364d5a"), Color.parseColor("#3f7178")}, null)
                    ;

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
                    grafica = findViewById(R.id.graficaLinear);
                    grafica.addData(dataset);
                    grafica.setAxisColor(Color.parseColor("#399699"))
                            .setYAxis(false)
                            .setYLabels(AxisRenderer.LabelPosition.NONE)
                            .setGrid(10, 20, paint)
                            .setAxisBorderValues(0, 100)
                            .setTypeface(Typeface.createFromAsset(esto.getAssets(), "OpenSans-Semibold.ttf"))
                            .setLabelsFormat(formato)
                            .setTooltips(mTip)
                            .show(new Animation().setInterpolator(new BounceInterpolator())
                                    .fromAlpha(0));

                    grafica.show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        this.finish();
        super.onDestroy();
    }
}
