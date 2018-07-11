package com.madcamp.dontknow.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.madcamp.dontknow.Activity.MainActivity;
import com.madcamp.dontknow.R;

import android.support.v4.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Tab2_ParentFragment extends Fragment {


    public int _num_registered;
    public List<String> num_registered = new ArrayList<String>();
    private ArrayList<Tab2_ChildFragment> children = new ArrayList<>();
    private FragmentTransaction transaction = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        update();
        return inflater.inflate(R.layout.tab2_fragment_parent, container, false);

    }

    public void update() {
        transaction = getChildFragmentManager().beginTransaction();
        for(int i=0;i<children.size();i++) {
            transaction.remove(children.get(i));
        }

        children = new ArrayList<>();
        num_registered = new ArrayList<>();

        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String url = "http://52.231.68.157:8080/api/registered/" + MainActivity.myTel;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try{
                    _num_registered = response.length();
                    num_registered.add(MainActivity.myTel);
                    for(int i = 0; i < _num_registered; i++){
                        num_registered.add(response.getString(i));
//                        System.out.println(num_registered.get(i));
                    }
                    kkk();
                } catch(JSONException e){
                }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(jsonArrayRequest);

        // Inflate the layout for this fragment
    }

    public void kkk() {

//        LinearLayout tab1_layout = getView().findViewById(R.id.tab1_layout);


        for(int i = 0; i < _num_registered+1; i++){

            Bundle bundle = new Bundle();
            Tab2_ChildFragment childFragment = new Tab2_ChildFragment();
            bundle.putString("owner_tel", num_registered.get(i));
            childFragment.setArguments(bundle);
            transaction.add(R.id.tab1_layout, childFragment);
            children.add(childFragment);
        }
        transaction.commit();
    }

}