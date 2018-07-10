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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.madcamp.dontknow.Activity.MainActivity;
import com.madcamp.dontknow.R;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ParentFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public int _num_registered;
    public List<String> num_registered = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String url = "http://52.231.68.157:8080/api/profiles/" + MainActivity.myTel;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try{
                    _num_registered = response.getJSONArray("registered").length();
                    for(int i = 0; i < _num_registered; i++){
                        num_registered.add(response.getJSONArray("registered").getString(i));
                    }
                    kkk();
                } catch(JSONException e){
                }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(jsonObjectRequest);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parent, container, false);

    }

    public void kkk() {

//        LinearLayout tab1_layout = getView().findViewById(R.id.tab1_layout);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        for(int i = 0; i < _num_registered; i++){
            Fragment childFragment = new ChildFragment();
            Bundle bundle = new Bundle();
            bundle.putString("owner_tel", num_registered.get(i));
            childFragment.setArguments(bundle);
            transaction.add(R.id.tab1_layout, childFragment);
//            System.out.println("jj");
        }
        transaction.commit();
//        transaction.replace(R.id.child_fragment_container, childFragment).commit();
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void messageFromParentFragment(Uri uri);
    }
}