package com.madcamp.dontknow.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.madcamp.dontknow.Activity.MainActivity;
import com.madcamp.dontknow.R;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChildFragment extends Fragment {
    String owner_name = "a";
    String user_tel= "1234";
//    public ChildFragment(String string){
//        owner_name = string;
//    }

    private OnFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_child, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.tab1_recyclerView);

        LinearLayoutManager tab1_recyclerView_layoutManager = new LinearLayoutManager(getContext());
        tab1_recyclerView_layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        Tab1RecyclerViewAdapter tab1_recyclerView_adapter = new Tab1RecyclerViewAdapter();


        Bitmap sample_bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.sample_image);
        for(int i=0; i<10; i++) {
            tab1_recyclerView_adapter.addItem(new UserGallery(sample_bitmap));
        }

        recyclerView.setLayoutManager(tab1_recyclerView_layoutManager);
        recyclerView.setAdapter(tab1_recyclerView_adapter);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String url = "http://52.231.68.157:8080/api/profiles/" + user_tel;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try{
                    System.out.println(response.getString("name"));
                    owner_name = response.getString("name");

                    TextView textView = getView().findViewById(R.id.owner_name);
                    textView.setText(owner_name);
                } catch(JSONException e){

                }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        queue.add(jsonObjectRequest);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

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
        void messageFromChildFragment(Uri uri);
    }



    class Tab1RecyclerViewAdapter extends RecyclerView.Adapter<Tab1RecyclerViewAdapter.Tab1RecyclerViewHolder>{

        ArrayList<UserGallery> userGalleries = new ArrayList<>();

        @NonNull
        @Override
        public Tab1RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i){
            return new Tab1RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_image, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Tab1RecyclerViewHolder viewHolder, final int position){
            Glide.with(viewHolder.itemView.getContext()).load(userGalleries.get(position).getImage()).into(viewHolder.imported_image);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
//                    Toast.makeText(view.getContext(), "Position : "+position, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return (userGalleries==null) ? 0 : userGalleries.size();
        }

        public void addItem(UserGallery gallery){
            userGalleries.add(gallery);
            notifyItemInserted(getItemCount()-1);
        }

        class Tab1RecyclerViewHolder extends RecyclerView.ViewHolder{
            ImageView imported_image;
            public Tab1RecyclerViewHolder(@NonNull View itemView){
                super(itemView);
                imported_image = itemView.findViewById(R.id.imported_image);

            }
        }
    }

    class UserGallery{
        private Bitmap image;
        public UserGallery(Bitmap _image) {
            image = _image;
        }
        public Bitmap getImage() {
            return image;
        }

    }
}