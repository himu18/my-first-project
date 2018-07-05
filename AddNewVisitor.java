package com.talent4assure.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.talent4assure.Visitor;
import com.talent4assure.R;
import com.talent4assure.model.Person;
import com.talent4assure.model.SubPerson;
import com.talent4assure.model.VisitorList;
import com.talent4assure.network.ApiUrl;
import com.talent4assure.network.RequestInterface;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddNewVisitor extends AppCompatActivity {

    ImageView imageView1;
    Button btnClick;

    private String TAG;
    Bitmap bitmap;

    static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int CAMERA_REQUEST = 1888;

    File compressedImageFile;
    List<VisitorList> visitorLists;
    List<SubPerson> subPeople;
    String mediaPath;
    File file;

    @BindView(R.id.et_new_name)
    EditText et_name;

    @BindView(R.id.et_new_email)
    EditText et_email;

    @BindView(R.id.et_new_ph_no)
    EditText ett_phno;

    @BindView(R.id.et_new_vId)
    EditText et_vId;

    @BindView(R.id.btn_gallery)
    Button btn_gallery;

    @BindView(R.id.submit_new)
    Button btn_submit;


    String visitor_name, visitor_email, visitor_mobile, visitor_id;

    AwesomeValidation awesomeValidation;
    String[] sp_purpose = {"Interview", "Meeting", "Vendor", "Personal"};

    Uri realURI;
    public static final int GALLEY_REQUEST_CODE = 1;
    String[] meeting_person;
    int[] meeting_person_id;
    String person_to_meet_name;
    MaterialBetterSpinner spr_person_to_meetnew;
    MaterialBetterSpinner spr_purpose_to_meetnew;
    int person_to_meet_id;
    int purpose_to_meet_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        awesomeValidation = new AwesomeValidation( ValidationStyle.BASIC );
        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
        setContentView( R.layout.activity_add_new_visitor );

        spr_purpose_to_meetnew = (MaterialBetterSpinner) findViewById( R.id.android_material_design_spinner_purpose_new );
        spr_person_to_meetnew = (MaterialBetterSpinner) findViewById( R.id.android_material_design_spinner_person_new );
        btnClick = findViewById( R.id.btn_camera );

        ButterKnife.bind( this );

        initviews();
        get_data();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>( getApplicationContext(), R.layout.spinner_list, sp_purpose );
        arrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        spr_purpose_to_meetnew.setAdapter( arrayAdapter );

        spr_purpose_to_meetnew.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                purpose_to_meet_id = i + 1;
            }
        } );

        spr_person_to_meetnew.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                person_to_meet_name = subPeople.get( i ).getFull_name();
                person_to_meet_id = subPeople.get( i ).getUserId();
            }
        } );


        btn_gallery.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent( Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                startActivityForResult( galleryIntent, 0 );
            }

        } );


        btn_submit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetworkAvailable()) {
                    if (awesomeValidation.validate()) {
                        {
                            if (mediaPath != null) {

                                visitor_name = et_name.getText().toString().trim();
                                visitor_email = et_email.getText().toString().trim();
                                visitor_mobile = ett_phno.getText().toString().trim();
                                visitor_id = et_vId.getText().toString().trim();

                                userSignup();
                            } else {
                                Toast.makeText( getApplicationContext(), "Select Image", Toast.LENGTH_LONG ).show();
                            }
                        }

                    } else {
                        Toast.makeText( getApplicationContext(), "please enter valid details", Toast.LENGTH_LONG ).show();
                    }
                } else {

                    snack_barmessage( "No Internet Connection!!!" );
                }

            }
        } );


        btnClick.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
                startActivityForResult( cameraIntent, CAMERA_REQUEST );
            }
        } );
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {

            if (requestCode == CAMERA_REQUEST) {
                Bitmap photo = (Bitmap) data.getExtras().get( "data" );
                imageView1.setImageBitmap( photo );

                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Uri tempUri = getImageUri( getApplicationContext(), photo );
                mediaPath = getRealPathFromURI( tempUri );
                // CALL THIS METHOD TO GET THE ACTUAL PATH
                file = new File( mediaPath );
            }
        }
        try {
            // When an Image is picked
            if (requestCode == 0 && resultCode == RESULT_OK && null != data) {

                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getApplicationContext().getContentResolver().query( selectedImage, filePathColumn, null, null, null );
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex( filePathColumn[0] );
                mediaPath = cursor.getString( columnIndex );

                imageView1.setImageBitmap( BitmapFactory.decodeFile( mediaPath ) );
                file = new File( mediaPath );
                cursor.close();


            } else {
                Toast.makeText( getApplicationContext(), "You haven't picked Image", Toast.LENGTH_LONG ).show();
            }
        } catch (Exception e) {
            Toast.makeText( getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG ).show();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress( Bitmap.CompressFormat.JPEG, 100, bytes );
        String path = MediaStore.Images.Media.insertImage( inContext.getContentResolver(), inImage, "Title", null );
        return Uri.parse( path );
    }


    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getApplicationContext().getContentResolver().query( contentUri, proj, null, null, null );
        assert cursor != null;
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow( MediaStore.Images.Media.DATA );
            res = cursor.getString( column_index );
        }
        cursor.close();
        return res;
    }*/

   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {

            if (requestCode == CAMERA_REQUEST) {
                Bitmap photo = (Bitmap) data.getExtras().get( "data" );
                imageView1.setImageBitmap( photo );

                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Uri tempUri = getImageUri( getApplicationContext(), photo );
                mediaPath = getRealPathFromURI( tempUri );
                // CALL THIS METHOD TO GET THE ACTUAL PATH
                file = new File( mediaPath );
            }


            try {
                // When an Image is picked
                if (requestCode == 0 && resultCode == RESULT_OK && null != data) {

                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getApplicationContext().getContentResolver().query( selectedImage, filePathColumn, null, null, null );
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex( filePathColumn[0] );
                    mediaPath = cursor.getString( columnIndex );
                    Log.e( "ghghg",mediaPath );

                    imageView1.setImageBitmap( BitmapFactory.decodeFile( mediaPath ) );
                  //  file = new File( mediaPath );
                    cursor.close();


                } else {
                    Toast.makeText( getApplicationContext(), "You haven't picked Image", Toast.LENGTH_LONG ).show();
                }
            } catch (Exception e) {
                Toast.makeText( getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG ).show();
            }

            try {
                // Get real path to make File
                realURI = Uri.parse( getRealPathFromURI( data.getData() ) );
                Log.d( TAG, "Image path :- " + realURI );
            } catch (Exception e) {
                Log.e( TAG, e.getMessage() );
            }
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
              bitmap = (Bitmap) data.getExtras().get( "data" );
                imageView1.setImageBitmap( bitmap );

            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress( Bitmap.CompressFormat.JPEG, 100, bytes );
        String path = MediaStore.Images.Media.insertImage( inContext.getContentResolver(), inImage, "Title", null );
        return Uri.parse( path );
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getApplicationContext().getContentResolver().query( contentUri, proj, null, null, null );
        assert cursor != null;
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow( MediaStore.Images.Media.DATA );
            res = cursor.getString( column_index );
        }
        cursor.close();
        return res;
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == GALLEY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageView1.setImageURI( data.getData() ); // set image to image view
            try {
                // Get real path to make File
                realURI = Uri.parse( getRealPathFromURI( data.getData() ) );
                Log.d( TAG, "Image path :- " + realURI );
            } catch (Exception e) {
                Log.e( TAG, e.getMessage() );
            }
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                bitmap = (Bitmap) data.getExtras().get( "data" );
                imageView1.setImageBitmap( bitmap );

                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Uri tempUri = getImageUri( getApplicationContext(), bitmap );
                mediaPath = getRealPathFromURI( tempUri );
                // CALL THIS METHOD TO GET THE ACTUAL PATH
                File file = new File( mediaPath );

            }
        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress( Bitmap.CompressFormat.JPEG, 100, bytes );
        String path = MediaStore.Images.Media.insertImage( inContext.getContentResolver(), inImage, "Title", null );
        return Uri.parse( path );
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getApplicationContext().getContentResolver().query( contentUri, proj, null, null, null );
        assert cursor != null;
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow( MediaStore.Images.Media.DATA );
            res = cursor.getString( column_index );
        }
        cursor.close();
        return res;
    }


    public void get_data() {
        final ProgressDialog progressDialog = new ProgressDialog( AddNewVisitor.this );
        progressDialog.setMessage( "Getting data..." );
        progressDialog.show();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( ApiUrl.BASE_URL )
                .addConverterFactory( GsonConverterFactory.create() )
                .build();
        RequestInterface service = retrofit.create( RequestInterface.class );
        Call<List<Person>> call = service.getperson();
        call.enqueue( new Callback<List<Person>>() {
            @Override
            public void onResponse(Call<List<Person>> call, Response<List<Person>> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {

                    subPeople = response.body().get( 0 ).getUSER();

                    meeting_person = new String[subPeople.size()];
                    meeting_person_id = new int[subPeople.size()];
                    for (int i = 0; i < subPeople.size(); i++) {
                        meeting_person[i] = subPeople.get( i ).getFull_name();
                        meeting_person_id[i] = subPeople.get( i ).getUserId();
                    }

                    ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>( getApplicationContext(), R.layout.spinner_list, meeting_person );
                    arrayAdapter2.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
                    spr_person_to_meetnew.setAdapter( arrayAdapter2 );

                } else {
                    Toast.makeText( getApplicationContext(), "else unresponse", Toast.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void onFailure(Call<List<Person>> call, Throwable t) {
                progressDialog.dismiss();
            }

        } );
    }

    public void userSignup() {
        final ProgressDialog progressDialog = new ProgressDialog( AddNewVisitor.this );
        progressDialog.setMessage( "Submitting Data..." );
        progressDialog.show();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout( 150, TimeUnit.SECONDS )
                .connectTimeout( 150, TimeUnit.SECONDS )
                .build();

        File file = new File( mediaPath );

        try {
            compressedImageFile = new Compressor( getApplicationContext() )
                    .setMaxWidth( 80 )
                    .setMaxHeight( 80 )
                    .compressToFile( file );
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create( MediaType.parse( "*/*" ), compressedImageFile );
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData( "file", compressedImageFile.getName(), requestBody );
        RequestBody filename = RequestBody.create( MediaType.parse( "text/plain" ), file.getName() );


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( ApiUrl.BASE_URL )
                .addConverterFactory( GsonConverterFactory.create() )
                .client( okHttpClient )
                .build();

        RequestInterface service1 = retrofit.create( RequestInterface.class );

        Call<List<VisitorList>> call = service1.addVisitor(
                visitor_name,
                visitor_email,
                visitor_mobile,
                visitor_id,
                person_to_meet_id,
                purpose_to_meet_id,
                fileToUpload );

        call.enqueue( new Callback<List<VisitorList>>() {
            @Override
            public void onResponse(Call<List<VisitorList>> call, Response<List<VisitorList>> response) {
                progressDialog.dismiss();

                visitorLists = response.body();

                if (response.isSuccessful()) {
                    int success = visitorLists.get( 0 ).getSUCCESS();
                    String message = visitorLists.get( 0 ).getMESSAGE();
                    if (success == 1) {
                        Toast.makeText( getApplicationContext(), message, Toast.LENGTH_LONG ).show();
                        Intent i = new Intent( getApplicationContext(), Visitor.class );
                        startActivity( i );
                        finish();

                    } else if (success == 2) {
                        Toast.makeText( getApplicationContext(), message, Toast.LENGTH_LONG ).show();

                    } else if (success == 3) {
                        Toast.makeText( getApplicationContext(), message, Toast.LENGTH_LONG ).show();

                    } else if (success == 0) {
                        Toast.makeText( getApplicationContext(), message, Toast.LENGTH_LONG ).show();

                    } else {
                        Toast.makeText( getApplicationContext(), "Plase try after some time", Toast.LENGTH_LONG ).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<VisitorList>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText( getApplicationContext(), visitorLists.get( 0 ).getMESSAGE(), Toast.LENGTH_LONG ).show();
            }
        } );
    }

    public void initviews() {

        imageView1 = (ImageView) findViewById( R.id.img_view_new );
        awesomeValidation.addValidation( this, R.id.android_material_design_spinner_purpose_new, RegexTemplate.NOT_EMPTY, R.string.Select_purpose );
        awesomeValidation.addValidation( this, R.id.android_material_design_spinner_person_new, RegexTemplate.NOT_EMPTY, R.string.Select_person );
        awesomeValidation.addValidation( this, R.id.et_new_name, RegexTemplate.NOT_EMPTY, R.string.Enter_Name );
        awesomeValidation.addValidation( this, R.id.et_new_ph_no, "^[+]?[0-9]{10,13}$", R.string.invalid_phone );
        awesomeValidation.addValidation( this, R.id.et_new_email, Patterns.EMAIL_ADDRESS, R.string.invalid_email );
        awesomeValidation.addValidation( this, R.id.et_new_vId, RegexTemplate.NOT_EMPTY, R.string.Enter_Valid_id );

    }


    public void snack_barmessage(String message) {

        Snackbar snackbar = Snackbar.make( findViewById( android.R.id.content ), message, Snackbar.LENGTH_LONG );
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById( android.support.design.R.id.snackbar_text );
        sbView.setBackgroundColor( ContextCompat.getColor( getApplicationContext(), R.color.black ) );
        textView.setTextColor( Color.YELLOW );
        snackbar.show();
    }


    private boolean isNetworkAvailable() {
        @SuppressLint("WrongConstant") ConnectivityManager connectivitymanager = (ConnectivityManager) getApplicationContext().getSystemService( "connectivity" );
        if (connectivitymanager != null) {
            NetworkInfo anetworkinfo[] = connectivitymanager.getAllNetworkInfo();
            if (anetworkinfo != null) {
                for (int i = 0; i < anetworkinfo.length; i++) {
                    if (anetworkinfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }

            }
        }
        return false;
    }
}
