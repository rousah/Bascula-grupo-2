package com.example.rousah.bascula;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import static com.example.rousah.bascula.Usuarios.guardarUsuario;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PerfilFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PerfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PerfilFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ImageView imagenPerfil;

    public PerfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PerfilFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PerfilFragment newInstance(String param1, String param2) {
        PerfilFragment fragment = new PerfilFragment();
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


        final View view = inflater.inflate(R.layout.perfil, container, false);
        final FragmentActivity fragmentActivity = getActivity();

        Button editar = view.findViewById(R.id.editar);
        editar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lanzarEditar();
            }
        });

        TextView nombre = view.findViewById(R.id.nombrePerfil);
        nombre.setText(usuario.getDisplayName());

        TextView email = view.findViewById(R.id.emailPerfil);
        email.setText(usuario.getEmail());

        imagenPerfil = view.findViewById(R.id.fotoPerfil);

        comprobarImagen();

        db.collection("usuarios").document(usuario.getUid()).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task){
                        if (task.isSuccessful()) {

                            TextView telefono = view.findViewById(R.id.telefono);
                            telefono.setText(task.getResult().getString("telefono"));

                            TextView sexo = view.findViewById(R.id.textoSexo);
                            /*sexo.setText(task.getResult().getString("sexo"));*/
                           if (task.getResult().getString("sexo").equals("masculino")) {
                                sexo.setText("Masculino");
                            }
                            if (task.getResult().getString("sexo").equals("femenino")) {
                                sexo.setText("Femenino");
                            }

                            TextView fechaNacimiento = view.findViewById(R.id.textoFecha);
                            fechaNacimiento.setText(task.getResult().getString("fechaNac"));

                        } else {
                            Log.e("Firestore", "Error al leer", task.getException());
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

    public void lanzarEditar(){
        Intent i = new Intent(getContext(), CrearPerfil.class);
        startActivity(i);
    }

    private void comprobarImagen() {
        final FirebaseUser usuario;
        usuario = FirebaseAuth.getInstance().getCurrentUser();

        //variables: imagen en Storage, uid del user actual y el proveedor de google
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String proveedor = usuario.getProviders().get(0);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        //imagenPerfil = headerLayout.findViewById(R.id.imagenNav);


        storageReference.child("imagenesPerfil/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getContext()).load(uri.toString()).resize(168, 168).centerCrop()
                        .transform(new CircleTransform())
                        .into(imagenPerfil);
                System.out.println("dentro de getPhoto");
                Toast.makeText(getContext(), uri.toString(), Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (proveedor.equals("google.com")) {
                    final String uri = usuario.getPhotoUrl().toString();
                    //carga la foto y usa transform para hacerla circular
                    Picasso.with(getContext()).load(uri).transform(new CircleTransform()).into(imagenPerfil);
                    System.out.println("dentro de getPhoto");
                    Toast.makeText(getContext(), "Googleado", Toast.LENGTH_LONG).show();
                } else {
                    //   imagenPerfil.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_account_circle_black_55dp, null));
                    Picasso.with(getContext()).load(R.drawable.round_account_circle_black_48dp).transform(new CircleTransform()).into(imagenPerfil);
                }
            }
        });
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
