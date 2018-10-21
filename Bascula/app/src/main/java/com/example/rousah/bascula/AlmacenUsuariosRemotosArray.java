//   BORJA
/*
VERSION: V 1.0.0
DESCRIPTION:
As the name says this class acts as a database for the remote users
So far they are inserted by hard, In a future it is expected they will be downloaded
from a data base.
The temporary storage is one array into the memory. The two methods defined so far aim
to handle that storade array
 */

package com.example.rousah.bascula;

import java.util.Vector;

public class AlmacenUsuariosRemotosArray implements AlmacenUsuariosRemotos {

    private Vector<String> usuariosRemotos;

    public AlmacenUsuariosRemotosArray() {
        usuariosRemotos = new Vector<String>();
        usuariosRemotos.add("New");
        usuariosRemotos.add("Manu Mouzo");
        usuariosRemotos.add("Carlos Canut");
        usuariosRemotos.add("Matthew Conde");
    }

    @Override
    public void guardarUsuariosRemotos(String nombre) {
        usuariosRemotos.add(nombre);
    }

    @Override
    public Vector<String> listarUsuariosRemotos(int cantidad) {
        return usuariosRemotos;
    }
}
