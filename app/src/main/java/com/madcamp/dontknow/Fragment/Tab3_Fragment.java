package com.madcamp.dontknow.Fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.madcamp.dontknow.Activity.MainActivity;
import com.madcamp.dontknow.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Tab3_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Tab3_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Tab3_Fragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";



    private GoogleMap mMap;

    public int _num_registered;
    public List<String> num_registered = new ArrayList<String>();
    public List<String> name_registered = new ArrayList<>();
    RequestQueue queue=null;
    String url = "http://52.231.68.157:8080/api/";

    public Tab3_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Tab3_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Tab3_Fragment newInstance(String param1, String param2) {
        Tab3_Fragment fragment = new Tab3_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    void addOnebyOne(final Tab3RecyclerViewAdapter adapter, final int index){
        if(index==_num_registered)
            return;
        queue.add(new StringRequest(Request.Method.GET, url+"name/"+num_registered.get(index), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                adapter.addItem(response);
                name_registered.add(response);
                addOnebyOne(adapter, index+1);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        }));
//        queue.add(stringRequest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("3 Made");
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        View view = inflater.inflate(R.layout.tab3_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.tab3_recyclerView);

        LinearLayoutManager tab3_layout_manager = new LinearLayoutManager(getContext());
        tab3_layout_manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        final Tab3RecyclerViewAdapter tab3_recycler_view_adapter = new Tab3RecyclerViewAdapter();


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url+"registered/"+MainActivity.myTel, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    _num_registered = response.length();
                    for(int i = 0; i < _num_registered; i++){
                        num_registered.add(response.getString(i));
//                        System.out.println(num_registered.get(i));
                    }
                    addOnebyOne(tab3_recycler_view_adapter, 0);
                } catch(JSONException e){
                }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(jsonArrayRequest);

        recyclerView.setLayoutManager(tab3_layout_manager);
        recyclerView.setAdapter(tab3_recycler_view_adapter);


        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
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
        void messageFromTab3Fragment(Uri uri);
    }

    class Tab3RecyclerViewAdapter extends RecyclerView.Adapter<Tab3RecyclerViewAdapter.Tab3RecyclerViewHolder>{
        ArrayList<String> userButtons = new ArrayList<>();

        @NonNull
        @Override
        public Tab3RecyclerViewAdapter.Tab3RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i){
            return new Tab3RecyclerViewAdapter.Tab3RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tab3_simple_user_button, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Tab3RecyclerViewAdapter.Tab3RecyclerViewHolder viewHolder, final int position) {
//            final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

            viewHolder.button.setText(userButtons.get(position));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(view.getContext(), "Position : " + position, Toast.LENGTH_SHORT).show();
                    String a = num_registered.get(position);
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url+"loc/"+a, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                LatLng latlng = new LatLng(Double.parseDouble(response.split(",")[0].trim()), Double.parseDouble(response.split(",")[1].trim()));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
                                mMap.addMarker(new MarkerOptions().position(latlng).title(name_registered.get(position)));
                            }
                            catch(Exception e) {
                                Toast.makeText(getContext(), "No location data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error);
                        }
                    });
                    queue.add(stringRequest);

                }
            });

        }

        @Override
        public int getItemCount() {
            return (userButtons==null) ? 0 : userButtons.size();
        }

        public void addItem(String s){

            userButtons.add(s);
            notifyItemInserted(getItemCount()-1);
        }

        class Tab3RecyclerViewHolder extends RecyclerView.ViewHolder{
            Button button;
            public Tab3RecyclerViewHolder(@NonNull View itemView){
                super(itemView);
                button = itemView.findViewById(R.id.user_button);
            }
        }
    }
}
