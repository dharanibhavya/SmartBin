package com.geekoders.smartbin;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.geekoders.smartbin.graphUtilities.GMapV2Direction;
import com.geekoders.smartbin.graphUtilities.TravellingSalesManproblem;
import com.geekoders.smartbin.graphUtilities.Vertex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kovbh01 on 8/5/2015.
 */
public class GoogleMapViewFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // LogCat tag
    private static final String TAG = GoogleMapViewFragment.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private AtomicInteger counter = new AtomicInteger();

    private Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;

    private GMapV2Direction md;

    private List<Vertex> vertices = new ArrayList<Vertex>();

    private int adj[][];

    private Document docAdj[][];

    private RequestQueue queue;

    private MapView mMapView;

    private GoogleMap googleMap;

    private TravellingSalesManproblem tsp;

    private EditText etLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_google_map_view, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();

        }

        md = new GMapV2Direction(getActivity());

        queue = Volley.newRequestQueue(getActivity());

        googleMap = mMapView.getMap();

        googleMap.setMyLocationEnabled(true);

        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }
        makeJsonObjectRequest();

        return v;

//        Button btn_find = (Button) v.findViewById(R.id.button);
//        etLocation = (EditText) v.findViewById(R.id.startLocation);
//
//        // Defining button click event listener for the find button
//        OnClickListener findClickListener = new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Getting user input location
//                String location = etLocation.getText().toString();
//
//                if(location!=null && !location.equals("")){
//                    new GeocoderTask().execute(location);
//                }else{
//                    makeJsonObjectRequest();
//                }
//            }
//        };
//        btn_find.setOnClickListener(findClickListener);

    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
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

    /**
     * Method to make json object request where json response starts wtih {
     */
    private void makeJsonObjectRequest() {
        final String urlJsonObj = "http://10.134.116.252:8080/TrackBins/track/bins/getLocations";
//        final String urlJsonObj = "http://192.168.1.103:8080/TrackBins/track/bins/getLocations";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
                urlJsonObj, (String) null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("bindetails");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject objects = array.getJSONObject(i);
                        double latitude = objects.getDouble("latitude");
                        double longitude = objects.getDouble("longitude");
                        double percent = objects.getDouble("percent");
                        addMarker(latitude, longitude, percent);
                    }
                    adj = new int[vertices.size()][vertices.size()];
                    docAdj = new Document[vertices.size()][vertices.size()];

                    generateAdjacencyMatrix();


                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        queue.add(jsonObjReq);
    }

    private void addMarker(double latitude, double longitude, double percent) {
        // create marker
        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(latitude, longitude)).title("Hello Maps");

        if (percent > 90) {
            // Changing marker icon
            marker.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED));
            vertices.add(new Vertex(latitude, longitude));
        } else if (percent > 50)
            // Changing marker icon
            marker.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        else {
            // Changing marker icon
            marker.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        // adding marker
        googleMap.addMarker(marker);
    }

    private void generateAdjacencyMatrix() {
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < vertices.size(); j++) {
                if (i != j) {
                    if (adj[i][j] == 0) {
                        md.getDocument(new LatLng(vertices.get(i).getLatitude(), vertices.get(i).getLongitude()),
                                new LatLng(vertices.get(j).getLatitude(), vertices.get(j).getLongitude()),
                                GMapV2Direction.MODE_DRIVING,
                                this, i, j);
                    }
                }
            }
        }
    }


    public void OnGetDistanceDocumentComplete(Document doc, int i, int j) {
        int distance = md.getDistanceValue(doc);
        adj[i][j] = distance;
        docAdj[i][j] = doc;

        if ((vertices.size() * vertices.size()) - vertices.size() == counter.incrementAndGet()) {
            findOptimalPath();
        }
    }

    private void findOptimalPath() {
        Toast.makeText(getActivity().getApplicationContext(), "Finding optimal path", Toast.LENGTH_SHORT).show();
        tsp = new TravellingSalesManproblem(vertices.size(), adj);
        List<Integer> pathVertex = tsp.execute();
        for (int i = 0; i < pathVertex.size() - 1; i++) {
            showOptimalPath(docAdj[pathVertex.get(i)][pathVertex.get(i + 1)]);
        }
    }


    public void showOptimalPath(Document doc) {
        ArrayList<LatLng> directionPoint = md.getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(8)
                .color(Color.BLUE);

        for (int i = 0; i < directionPoint.size(); i++) {
            rectLine.add(directionPoint.get(i));
        }
        googleMap.addPolyline(rectLine);
    }

    /**
     * Method to display the location on UI
     */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            vertices.add(new Vertex(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity().getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

//    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {
//
//        @Override
//        protected List<Address> doInBackground(String... locationName) {
//            // Creating an instance of Geocoder class
//            Geocoder geocoder = new Geocoder(getActivity().getBaseContext());
//            List<Address> addresses = null;
//
//            try {
//                // Getting a maximum of 3 Address that matches the input text
//                addresses = geocoder.getFromLocationName(locationName[0], 3);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return addresses;
//        }
//
//        @Override
//        protected void onPostExecute(List<Address> addresses) {
//
//            if (addresses == null || addresses.size() == 0) {
//                Toast.makeText(getActivity().getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
//            }
//
//            // Clears all the existing markers on the map
//            googleMap.clear();
//
//            // Adding Markers on Google Map for each matching address
//            for (int i = 0; i < addresses.size(); i++) {
//
//                Address address = addresses.get(i);
//                if (i == 0){
//                CameraPosition cameraPosition = new CameraPosition.Builder()
//                        .target(new LatLng(address.getLatitude(), address.getLongitude())).zoom(12).build();
//                googleMap.animateCamera(CameraUpdateFactory
//                        .newCameraPosition(cameraPosition));}
//
//            }
////            makeJsonObjectRequest();
//        }
//    }
}