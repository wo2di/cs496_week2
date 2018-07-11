package com.madcamp.dontknow.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.madcamp.dontknow.Activity.MainActivity;
import com.madcamp.dontknow.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Tab1_Fragment extends Fragment {

    public Uri currentUri;
    String mCurrentPhotoPath;
    public static final int PROFILE_CAMERA = 0;
    public static final int PROFILE_ALBUM = 1;
    private ImageView imageView;
    private Tab1RecyclerViewAdapter _adapter;




    RequestQueue queue = null;
    RecyclerView recView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1_fragment, container, false);
        recView = view.findViewById(R.id.tab1_RecView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recView.setLayoutManager(layoutManager);
        final Tab1RecyclerViewAdapter adapter = new Tab1RecyclerViewAdapter();
        recView.setAdapter(adapter);
        setUpRecView(adapter);
        _adapter = adapter;

        // Setup floating action buttons
        final FloatingActionButton main_FAB = view.findViewById(R.id.main_FAB);
        final FloatingActionButton editName_FAB = view.findViewById(R.id.editName_FAB);
        final FloatingActionButton searchFriend_FAB = view.findViewById(R.id.searchFriend_FAB);

        main_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editName_FAB.getVisibility() == View.GONE) {


                    editName_FAB.setVisibility(View.VISIBLE);
                    editName_FAB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new NameEditDialog(getContext(), adapter).show();
                        }
                    });
                    searchFriend_FAB.setVisibility(View.VISIBLE);
                    searchFriend_FAB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new FindAddDialog(getContext(), adapter).show();
                        }
                    });


                    main_FAB.setImageResource(R.drawable.cancel_icon);
                } else {
                    editName_FAB.setVisibility(View.GONE);
                    searchFriend_FAB.setVisibility(View.GONE);
                    main_FAB.setImageResource(R.drawable.add_icon);
                }
            }
        });


        return view;
    }

    void setUpRecView(final Tab1RecyclerViewAdapter adapter) {
        queue = Volley.newRequestQueue(getContext());
        queue.add(new StringRequest(MainActivity.url + "/mainImage/" + MainActivity.myTel, new Response.Listener<String>() {
            @Override
            public void onResponse(final String mainImage) {
                queue.add(new StringRequest(MainActivity.url + "/name/" + MainActivity.myTel, new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String name) {
                        byte[] decodedImage = Base64.decode(mainImage, Base64.DEFAULT);
                        adapter.addItem(new UserProfile(BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length), "나(" + name + ")", MainActivity.myTel));
                        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(MainActivity.url + "/registered/" + MainActivity.myTel,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        addItemOneByOne(adapter, response, 0);
                                    }
                                }, null);
                        queue.add(jsonArrayRequest);
                    }
                }, null));
            }
        }, null));
    }

    void addItemOneByOne(final Tab1RecyclerViewAdapter adapter, final JSONArray jsonArray, final int index) {
        if (jsonArray.length() == index)
            return;
        try {
            queue.add(new StringRequest(MainActivity.url + "/mainImage/" + jsonArray.getString(index), new Response.Listener<String>() {
                @Override
                public void onResponse(final String mainImage) {
                    try {
                        queue.add(new StringRequest(MainActivity.url + "/name/" + jsonArray.getString(index), new Response.Listener<String>() {
                            @Override
                            public void onResponse(final String name) {
                                byte[] decodedImage = Base64.decode(mainImage, Base64.DEFAULT);
                                try {
                                    adapter.addItem(new UserProfile(BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length), name, jsonArray.getString(index)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                addItemOneByOne(adapter, jsonArray, index + 1);
                            }
                        }, null));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // FOR CAMERA AND ALBUM HANDLING
    void profile_captureCamera() {
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        currentUri = getProviderUri();
        i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, currentUri);
        startActivityForResult(i, PROFILE_CAMERA);
    }
    void profile_getAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PROFILE_ALBUM);
    }
    Uri getProviderUri() {
        return FileProvider.getUriForFile(getContext(), getActivity().getPackageName(), createImageFile());
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
                Bitmap temp_result = rotateBitmap(captureBmp);
                final Bitmap result = Bitmap.createScaledBitmap(temp_result, 500, 500*temp_result.getHeight()/temp_result.getWidth(), false);
                imageView.setImageBitmap(result);


                // UPLOAD MAIN IMAGE
                Volley.newRequestQueue(getContext()).add(new StringRequest(Request.Method.PUT,
                        MainActivity.url + "/profiles/" + _adapter.users.get(0).getTel(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getContext(), "Uploaded",Toast.LENGTH_SHORT).show();
                            }
                        }, null) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> myData = new HashMap<>();

                        ByteArrayOutputStream baos = null;
                        try {
                            baos = new ByteArrayOutputStream();
                            result.compress(Bitmap.CompressFormat.PNG, 100, baos);
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
                        String encodedStr = Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);


                        myData.put("mainImage", encodedStr);

                        return myData;
                    }
                });


                break;
            }
            case 1 : {
                Bitmap bitmap = null;
                Uri image = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                Bitmap temp_result = rotateBitmap(bitmap);
                final Bitmap result = Bitmap.createScaledBitmap(temp_result, 500, 500*temp_result.getHeight()/temp_result.getWidth(), false);
                imageView.setImageBitmap(result);


                // UPLOAD MAIN IMAGE
                Volley.newRequestQueue(getContext()).add(new StringRequest(Request.Method.PUT,
                        MainActivity.url + "/profiles/" + _adapter.users.get(0).getTel(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getContext(), "Uploaded",Toast.LENGTH_SHORT).show();
                            }
                        }, null) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> myData = new HashMap<>();

                        ByteArrayOutputStream baos = null;
                        try {
                            baos = new ByteArrayOutputStream();
                            result.compress(Bitmap.CompressFormat.PNG, 100, baos);
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
                        String encodedStr = Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);


                        myData.put("mainImage", encodedStr);

                        return myData;
                    }
                });

                break;
            }

        }
    }



    class Tab1RecyclerViewAdapter extends RecyclerView.Adapter<Tab1RecyclerViewAdapter.RecyclerViewHolder> {
        ArrayList<UserProfile> users = new ArrayList<>();


        @NonNull
        @Override
        public Tab1RecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new Tab1RecyclerViewAdapter.RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tab1_user_profile, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final Tab1RecyclerViewAdapter.RecyclerViewHolder viewHolder, final int position) {
            viewHolder.setUserProfile(users.get(position));
            Glide.with(viewHolder.itemView.getContext()).load(users.get(position).getImage()).into(viewHolder.profile_picture_view);
            //Glide.with(viewHolder.itemView.getContext()).load(BitmapFactory.decodeResource(getResources(), R.drawable.seohyeon)).into(viewHolder.profile_picture_view);
            viewHolder.profile_name_view.setText(users.get(position).getName());
            viewHolder.profile_tel_view.setText(users.get(position).getTel());
            ImageView erase_button = viewHolder.itemView.findViewById(R.id.erase_button);
            imageView = viewHolder.itemView.findViewById(R.id.profile_picture);



            // SET PROFILE IMAGE
            Volley.newRequestQueue(getContext()).add(new StringRequest(Request.Method.GET,
                    MainActivity.url + "/mainImage/" + users.get(position).getTel(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    byte[] decodedImage = Base64.decode(response, Base64.DEFAULT);
                    Bitmap result = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                    imageView.setImageBitmap(result);
                    System.out.println("DONE WELL");
                }
            }, null));



            if(position==0) {
                erase_button.setOnClickListener(null);
                erase_button.setVisibility(View.GONE);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageView=(ImageView)view;
                        final String[] options = new String[]{"사진 촬영", "앨범에서 사진 선택", "기본 이미지로 변경"};
                        AlertDialog.Builder pDialog = new AlertDialog.Builder(getContext());
                        pDialog.setTitle("프로필 사진 변경");
                        pDialog.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        //카메라
                                        profile_captureCamera();
                                        break;
                                    case 1:
                                        //앨범
                                        profile_getAlbum();
                                        break;
                                    case 2:
                                        //기본이미지
                                        imageView.setImageResource(R.drawable.default_image);

                                        final Bitmap result = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);

                                        Volley.newRequestQueue(getContext()).add(new StringRequest(Request.Method.PUT,
                                                MainActivity.url + "/profiles/" + _adapter.users.get(0).getTel(),
                                                new Response.Listener<String>() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        Toast.makeText(getContext(), "Uploaded",Toast.LENGTH_SHORT).show();
                                                    }
                                                }, null) {
                                            @Override
                                            protected Map<String, String> getParams() {
                                                Map<String, String> myData = new HashMap<>();

                                                ByteArrayOutputStream baos = null;
                                                try {
                                                    baos = new ByteArrayOutputStream();
                                                    result.compress(Bitmap.CompressFormat.PNG, 100, baos);
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
                                                String encodedStr = Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);


                                                myData.put("mainImage", encodedStr);

                                                return myData;
                                            }
                                        });

                                        break;
                                }
                            }
                        });
                        pDialog.show();

                    }
                });
            }
            if (position != 0) {
                imageView.setOnClickListener(null);
                erase_button.setVisibility(View.VISIBLE);
                erase_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int index = 0;
                            for (; index < users.size(); index++) {
                                if (users.get(index) == viewHolder.getUserProfile())
                                    break;
                            }

                            if (index == 0)
                                return;

                            final int target = index;

                            Volley.newRequestQueue(getContext()).add(new StringRequest(Request.Method.DELETE,
                                    MainActivity.url + "/registered/" + users.get(0).getTel() + "/" + (target - 1),
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            users.remove(target);
                                            notifyItemRemoved(target);
                                            ((MainActivity)getActivity()).tab2_parentFragment.update();
                                        }
                                    }, null));



                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Never dies haha");
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return (users == null) ? 0 : users.size();
        }

        public void addItem(UserProfile profile) {
            users.add(profile);
            notifyItemInserted(getItemCount() - 1);
        }


        //TODO : Get image, name and tel from itemView
        class RecyclerViewHolder extends RecyclerView.ViewHolder {
            UserProfile userProfile = null;
            ImageView profile_picture_view;
            TextView profile_name_view;
            TextView profile_tel_view;

            public RecyclerViewHolder(@NonNull View itemView) {
                super(itemView);
                profile_picture_view = itemView.findViewById(R.id.profile_picture);
                profile_name_view = itemView.findViewById(R.id.profile_name);
                profile_tel_view = itemView.findViewById(R.id.profile_tel);
            }

            public void setUserProfile(UserProfile userProfile) {
                this.userProfile = userProfile;
            }

            public UserProfile getUserProfile() {
                return userProfile;
            }
        }
    }

    class UserProfile {
        private Bitmap image;
        private String name;
        private String tel;

        public UserProfile(Bitmap _image, String _name, String _tel) {
            image = _image;
            name = _name;
            tel = _tel;
        }

        public Bitmap getImage() {
            return image;
        }

        public String getName() {
            return name;
        }

        public String getTel() {
            return tel;
        }

        public void setImage(Bitmap _image) {
            image = _image;
        }

        public void setName(String _name) {
            name = _name;
        }

        public void setTel(String _tel) {
            tel = _tel;
        }
    }

    class NameEditDialog extends Dialog {
        Tab1RecyclerViewAdapter adapter = null;

        public NameEditDialog(@NonNull Context _context, Tab1RecyclerViewAdapter _adapter) {
            super(_context);
            adapter = _adapter;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.tab1_name_edit_dialog);
            Button edit_button = findViewById(R.id.edit_button);
            Button cancel_button = findViewById(R.id.cancel_button);
            edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String newName = ((EditText) findViewById(R.id.new_name)).getText().toString();

                    adapter.users.get(0).setName("나(" + newName + ")");

                    Volley.newRequestQueue(getContext()).add(new StringRequest(Request.Method.PUT, MainActivity.url + "/profiles/" + MainActivity.myTel, null, null) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> myData = new HashMap<>();
                            myData.put("name", newName);
                            return myData;
                        }
                    });

                    adapter.notifyItemChanged(0);
                    NameEditDialog.this.cancel();
                }
            });
            cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NameEditDialog.this.cancel();
                }
            });
        }
    }

    class FindAddDialog extends Dialog {
        Tab1RecyclerViewAdapter adapter = null;

        public FindAddDialog(@NonNull Context _context, Tab1RecyclerViewAdapter _adapter) {
            super(_context);
            adapter = _adapter;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.tab1_find_add_dialog);
            Button add_button = findViewById(R.id.add_button);
            Button cancel_button = findViewById(R.id.cancel_button);
            add_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String newTel = ((EditText) findViewById(R.id.phone_number)).getText().toString();
                    final String code = ((EditText) findViewById(R.id.code)).getText().toString();
                    System.out.println("newTel : "+newTel);
                    System.out.println("code : "+code);

                    Volley.newRequestQueue(getContext()).add(new StringRequest(Request.Method.PUT, MainActivity.url + "/registered/" + MainActivity.myTel,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    JSONObject json = null;
                                    try {
                                        json = new JSONObject(response);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    String message = null;
                                    try {
                                        message = json.getString("message");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("Message : " + message);
                                    if (message != null && message.equals("profile updated")) {

                                        addItemOneByOne(adapter, new JSONArray().put(newTel), 0);
                                        Toast.makeText(getContext(), "Added successfully", Toast.LENGTH_SHORT).show();
                                        ((MainActivity)getActivity()).tab2_parentFragment.update();
                                    } else if(message != null && message.equals("already exists")) {
                                        Toast.makeText(getContext(), "Already added!", Toast.LENGTH_SHORT).show();
                                    }
                                        else {
                                        Toast.makeText(getContext(), "Wrong number or code", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), "Wrong number or code", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> myData = new HashMap<>();
                            myData.put("tel", newTel);
                            myData.put("code", code);
                            return myData;
                        }
                    });

                    FindAddDialog.this.cancel();
                }
            });
            cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FindAddDialog.this.cancel();
                }
            });
        }
    }
}

