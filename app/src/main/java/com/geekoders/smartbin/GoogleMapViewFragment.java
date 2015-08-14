package com.geekoders.smartbin;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kovbh01 on 8/5/2015.
 */
public class GoogleMapViewFragment extends Fragment {

    private GMapV2Direction md;

    private List<Vertex> vertices = new ArrayList<Vertex>();

    private List<Vertex> path = new LinkedList<Vertex>();

    private int adj[][];

    private Document docAdj[][];

    private RequestQueue queue;

    private MapView mMapView;

    private GoogleMap googleMap;

    private TravellingSalesManproblem tsp;

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

        makeJsonObjectRequest();

        return v;
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

    /**
     * Method to make json object request where json response starts wtih {
     */
    private void makeJsonObjectRequest() {
        // json object response url
        final String urlJsonObj = "http://10.134.116.252:8080/TrackServer/track/bins/getLocations";
//        final String urlJsonObj = "http://192.168.1.100:8080/TrackServer/track/bins/getLocations";

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
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(17.45, 78.45)).zoom(12).build();
                    googleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));

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
        if (i == vertices.size() - 1 && j == vertices.size() - 2) {
            findOptimalPath();
        }
    }

    private void findOptimalPath() {
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
}
