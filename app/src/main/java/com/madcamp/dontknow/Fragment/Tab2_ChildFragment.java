package com.madcamp.dontknow.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.madcamp.dontknow.Activity.MainActivity;
import com.madcamp.dontknow.R;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Tab2_ChildFragment extends Fragment {
    String owner_tel ="owner_tel";
    String owner_name = "owner_name";
    //    Bitmap owner_pic;
    String url = "http://52.231.68.157:8080/api/";
    RequestQueue queue=null;
    public Uri currentUri;
    String mCurrentPhotoPath;
    RecyclerView recyclerView;

    public static final int FROM_CAMERA = 0;
    public static final int FROM_ALBUM = 1;
    final Tab2RecyclerViewAdapter tab2_recyclerView_adapter = new Tab2RecyclerViewAdapter();

    private int change_position;

    void addOneByOne(final Tab2RecyclerViewAdapter adapter, final int index) {
        if(index==5)
            return;
        queue.add(new StringRequest(Request.Method.GET, url + "background"+ Integer.toString(index+1) +"/"+ owner_tel, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                byte[] decodedString = Base64.decode(response, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                adapter.addItem(decodedByte);
                addOneByOne(adapter, index+1);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        owner_tel = getArguments().getString("owner_tel");
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        final View view = inflater.inflate(R.layout.tab2_fragment_child, container, false);
        recyclerView = view.findViewById(R.id.tab2_recyclerView);

        LinearLayoutManager tab2_recyclerView_layoutManager = new LinearLayoutManager(getContext());
        tab2_recyclerView_layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        recyclerView.setLayoutManager(tab2_recyclerView_layoutManager);
        recyclerView.setAdapter(tab2_recyclerView_adapter);

        addOneByOne(tab2_recyclerView_adapter, 0);

        if(!owner_tel.equals(MainActivity.myTel)) {
            StringRequest StringRequest = new StringRequest(Request.Method.GET, url + "name/" + owner_tel, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    owner_name = response;
                    TextView textView = view.findViewById(R.id.owner_name);
                    textView.setText(owner_name);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            queue.add(StringRequest);
            return view;
        }

        ((TextView)view.findViewById(R.id.owner_name)).setText("나");
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!(resultCode== Activity.RESULT_OK))
            return;
        switch(requestCode) {
            case 0 : {
                Bitmap captureBmp=null;
                try{
                    captureBmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),currentUri);
                }catch(Exception e) {
                    e.printStackTrace();
                }

                captureBmp = rotateBitmap(captureBmp);
                captureBmp = Bitmap.createScaledBitmap(captureBmp, 300, 300*captureBmp.getHeight()/captureBmp.getWidth(), false);
                uploadByPut(captureBmp, change_position);
                tab2_recyclerView_adapter.changeItem(captureBmp, change_position );

//                ((MainActivity)getActivity()).getServerData("UPLOAD_BACKGROUND", captureBmp);
                recyclerView.setAdapter(tab2_recyclerView_adapter);
                break;
            }
            case 1 : {
                Bitmap galleryBmp = null;
                Uri image = data.getData();
                try {
                    galleryBmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
                } catch(Exception e) {
                    e.printStackTrace();
                }

                galleryBmp = rotateBitmap(galleryBmp);
                galleryBmp = Bitmap.createScaledBitmap(galleryBmp, 300, 300*galleryBmp.getHeight()/galleryBmp.getWidth(), false);
                uploadByPut(galleryBmp, change_position);
                tab2_recyclerView_adapter.changeItem(galleryBmp, change_position );

                break;
            }
        }
    }


    class Tab2RecyclerViewAdapter extends RecyclerView.Adapter<Tab2RecyclerViewAdapter.Tab2RecyclerViewHolder>{
        ArrayList<Bitmap> userGalleries = new ArrayList<>();

        @NonNull
        @Override
        public Tab2RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i){
            return new Tab2RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tab2_simple_image, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Tab2RecyclerViewHolder viewHolder, final int position){
            Glide.with(viewHolder.itemView.getContext()).load(userGalleries.get(position)).into(viewHolder.imported_image);
            if(owner_tel.equals(MainActivity.myTel)) {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                    Toast.makeText(view.getContext(), "Position : "+position, Toast.LENGTH_SHORT).show();

                        final String[] options = new String[]{"카메라", "앨범", "기본 이미지"};
                        AlertDialog.Builder pDialog = new AlertDialog.Builder(Tab2_ChildFragment.this.getContext());
//                        AlertDialog.Builder pDialog = new AlertDialog.Builder(Tab2_ChildFragment.this.getContext());
                        pDialog.setTitle("사진 변경");
                        pDialog.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                change_position = position;
                                switch(which){
                                    case 0:
                                        fromCamera();
                                        break;
                                    case 1:
                                        fromAlbum();
                                        break;
                                    case 2:
                                        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.default_image);
                                        bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300*bitmap.getHeight()/bitmap.getWidth(), false);
                                        uploadByPut(bitmap, change_position);
                                        tab2_recyclerView_adapter.changeItem(bitmap, change_position );

//                                        iv_view.setImageResource(R.drawable.usericon);
                                        break;
                                }
                            }
                        });
                        pDialog.setPositiveButton("취소", null);
                        pDialog.show();

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return (userGalleries==null) ? 0 : userGalleries.size();
        }

        public void addItem(Bitmap image){
            userGalleries.add(image);
            notifyItemInserted(getItemCount()-1);
        }

        public void changeItem(Bitmap image, int position){
            userGalleries.set(position, image);
            notifyItemChanged(position);
        }

        class Tab2RecyclerViewHolder extends RecyclerView.ViewHolder{
            ImageView imported_image;
            public Tab2RecyclerViewHolder(@NonNull View itemView){
                super(itemView);
                imported_image = itemView.findViewById(R.id.imported_image);

            }
        }
    }

    public void fromCamera(){
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        currentUri = getProviderUri();
        i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, currentUri);
        startActivityForResult(i, FROM_CAMERA);
    }
    public void fromAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, FROM_ALBUM);

    }

    Uri getProviderUri() {
        System.out.println(getContext());
        System.out.println(getActivity().getPackageName());
        System.out.println(createImageFile());
        return FileProvider.getUriForFile(getContext(), getActivity().getPackageName() ,createImageFile());
    }

    public File createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyymmdd_hhmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "gyeom");

        if(!storageDir.exists()){
            Log.i("mCurrentPhotoPath", storageDir.toString());
            storageDir.mkdirs();
        }
        File imageFile = new File(storageDir,imageFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    public Bitmap rotateBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        return rotatedBitmap;
    }

    public void uploadByPut(final Bitmap bitmap, final int position){

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url+"profiles/"+owner_tel, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getView().getContext(), "Image saved", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                ByteArrayOutputStream baos = null;
                try {
                    baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                } finally {
                    if (baos != null) {
                        try {
                            baos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                //MyData.put("mainImage", Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT));
                String str = Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);
                System.out.println("background"+Integer.toString(position+1));
//                System.out.println(str);
                MyData.put("background"+Integer.toString(position+1), str);
                return MyData;
            }
        };
        queue.add(stringRequest);
    }
}