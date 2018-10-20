//  BORJA
/*
VERSION: V 1.0.0
DESCRIPTION:
Initial
 */

package com.example.rousah.bascula;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UsuariosRemotosActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usuarios_remotos);
        setListAdapter(new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                MainActivity.almacen.listarUsuariosRemotos(10)));
    }

    @Override
    protected void onListItemClick(ListView listView,
                                   View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Object o = getListAdapter().getItem(position);


        Intent intent = new Intent(this, UsuarioRemotoActivity.class);
        String data = o.toString();
        
        intent.putExtra("usuarioremoto", data);

        // Start activity of communication
        startActivity(intent);
         finish();



    }
}
