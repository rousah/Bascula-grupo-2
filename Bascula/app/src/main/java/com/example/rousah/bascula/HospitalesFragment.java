package com.example.rousah.bascula;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HospitalesFragment extends Fragment {
    MapView mMapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationClient;
    double longitud;
    double latitud;
    LatLng me;
    LocationManager locationManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hospitales, container, false);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(),     getResources().getString(R.string.solicitudPermisoMapa), Toast.LENGTH_SHORT);
                    Intent i = new Intent(getContext(), MainActivity.class);
                    startActivity(i);
                    return;
                }

                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    Log.d("mapa", location.toString());
                                    longitud = location.getLongitude();
                                    latitud = location.getLatitude();
                                    me = new LatLng(latitud, longitud);

                                    // For zooming automatically to the location of the marker
                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(me).zoom(12).build();
                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                    StringBuilder sbValue = new StringBuilder(sbMethod());
                                    PlacesTask placesTask = new PlacesTask();
                                    placesTask.execute(sbValue.toString());
                                }
                            }
                        });

                // For showing a move to my location button
                googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng sydney = new LatLng(-34, 151);

                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public StringBuilder sbMethod() {

         SharedPreferences pref =
                 PreferenceManager.getDefaultSharedPreferences(getContext());
         String placeToLookFor;
         if(pref.getString("localizacion","?").equals("1")) {
             placeToLookFor = "hospital";
         }else{
             placeToLookFor = "pharmacy";
         }


        double mLatitude = latitud;
        double mLongitude = longitud;

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius=5000");
        sb.append("&types=" + placeToLookFor);
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyDXWcRvjtT6v6ElStSAsAOB_Bav6TAlhjQ");

        Log.d("Map", "api: " + sb.toString());

        return sb;
    }

    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParserTask
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception downloading", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            Place_JSON placeJson = new Place_JSON();

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            Log.d("Map", "list size: " + list.size());
            // Clears all the existing markers;
            googleMap.clear();

            for (int i = 0; i < list.size(); i++) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);


                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                String name = hmPlace.get("place_name");

                Log.d("Map", "place: " + name);

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                markerOptions.title(name + " : " + vicinity);

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                // Placing a marker on the touched position
                Marker m = googleMap.addMarker(markerOptions);

            }
        }
    }

    public class Place_JSON {

        /**
         * Receives a JSONObject and returns a list
         */
        public List<HashMap<String, String>> parse(JSONObject jObject) {

            JSONArray jPlaces = null;
            try {
                /** Retrieves all the elements in the 'places' array */
                jPlaces = jObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /** Invoking getPlaces with the array of json object
             * where each json object represent a place
             */
            return getPlaces(jPlaces);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> place = null;

            /** Taking each place, parses and adds to list object */
            for (int i = 0; i < placesCount; i++) {
                try {
                    /** Call getPlace with place JSON object to parse the place */
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return placesList;
        }

        /**
         * Parsing the Place JSON object
         */
        private HashMap<String, String> getPlace(JSONObject jPlace) {

            HashMap<String, String> place = new HashMap<String, String>();
            String placeName = "-NA-";
            String vicinity = "-NA-";
            String latitude = "";
            String longitude = "";
            String reference = "";

            try {
                // Extracting Place name, if available
                if (!jPlace.isNull("name")) {
                    placeName = jPlace.getString("name");
                }

                // Extracting Place Vicinity, if available
                if (!jPlace.isNull("vicinity")) {
                    vicinity = jPlace.getString("vicinity");
                }

                latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
                reference = jPlace.getString("reference");

                place.put("place_name", placeName);
                place.put("vicinity", vicinity);
                place.put("lat", latitude);
                place.put("lng", longitude);
                place.put("reference", reference);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return place;
        }
    }

}
