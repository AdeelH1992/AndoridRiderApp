package com.example.lenovossd.andoridriderapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovossd.andoridriderapp.Common.Common;
import com.example.lenovossd.andoridriderapp.Helper.CustomInfoWindow;
import com.example.lenovossd.andoridriderapp.Model.FCMResponse;
import com.example.lenovossd.andoridriderapp.Model.Notification;
import com.example.lenovossd.andoridriderapp.Model.Rider;
import com.example.lenovossd.andoridriderapp.Model.Sender;
import com.example.lenovossd.andoridriderapp.Model.Token;
import com.example.lenovossd.andoridriderapp.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.io.PipedInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback
,GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener
,        com.google.android.gms.location.LocationListener
{

    SupportMapFragment mapFragment;


    //Location

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE =7192;

    private static final int PLAY_SERVICE_RES_REQUEST = 300193;


    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;

    GeoFire geoFire;

    Marker mUserMarker,markerDestination;

    // bottom Sheet
    ImageView imgExpandable;
   // BottomSheetRiderFragment mBottomSheet;

    Button btnPickUpRequest;


    int radius = 1 ; // 1 km

    int distance = 1 ;// 3 km
    private static final int LIMIT  = 3;

    //Send Alert

    IFCMService mService;


    // presence system

    DatabaseReference driverAvailable;

    PlaceAutocompleteFragment place_location,place_destination;

    AutocompleteFilter typeFilter;

    String mPlaceLocation,mPlaceDestination;

    // new Update Inforamtion

    CircleImageView imageAvatar;
    TextView txtRiderName,txtStars;

    // Firebase Storage to upload avatar

    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mService= Common.getFCMService();

        // init Storage

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawer.addDrawerListener( toggle );
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );

        // Map

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );


        // init Views imageavatar , rider name , riderstars

        View navigationHeaderView = navigationView.getHeaderView( 0 );

        txtRiderName = navigationHeaderView.findViewById( R.id.txtRiderName );
        txtRiderName.setText( String.format( "%s",Common.currentUser.getName() ) );
        txtStars = navigationHeaderView.findViewById( R.id.txtStars );
        txtStars.setText( String.format( "%s",Common.currentUser.getRates() ) );

        imageAvatar = navigationHeaderView.findViewById( R.id.imageAvatar );


        //Load image

        if (Common.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty( Common.currentUser.getAvatarUrl() ))
        {
            Picasso.with( this )
                    .load( Common.currentUser.getAvatarUrl() )
                    .into( imageAvatar );
        }





        //init View



        btnPickUpRequest = (Button)findViewById( R.id.btnPickupRequest );
       btnPickUpRequest.setOnClickListener( new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               if(!Common.isDriverFound)
                   requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
               else
                   sendRequestToDriver(Common.driverId);
           }
       } );


       place_destination = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById( R.id.place_destination );
        place_location = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById( R.id.place_location );

        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter( AutocompleteFilter.TYPE_FILTER_ADDRESS )
                .setTypeFilter( 3 )
                .build();

        //Event

         place_location.setOnPlaceSelectedListener( new PlaceSelectionListener() {
             @Override
             public void onPlaceSelected(Place place) {

                 mPlaceLocation = place.getAddress().toString();

                 // Remove old marker

                 mMap.clear();

                 mUserMarker = mMap.addMarker( new MarkerOptions().position( place.getLatLng() )
                 .icon( BitmapDescriptorFactory.defaultMarker() )
                 .title( "Pickup Here" ));

                 mMap.animateCamera( CameraUpdateFactory.newLatLngZoom( place.getLatLng(),15.0f ) );
             }

             @Override
             public void onError(Status status) {

             }
         } );

        place_destination.setOnPlaceSelectedListener( new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                mPlaceDestination = place.getAddress().toString();


                    // add new marker
                mMap.addMarker( new MarkerOptions().position( place.getLatLng() )
                .icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_BLUE ) ));

                mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f ) );

                BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance( mPlaceLocation,mPlaceDestination,false );
                Home.super.onPostResume();
              mBottomSheet.show( getSupportFragmentManager(),mBottomSheet.getTag());

            }

            @Override
            public void onError(Status status) {

            }
        } );

        setUpLoaction();

        updateFirebaseToken();
    }

    private void updateFirebaseToken() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        DatabaseReference tokens = db.getReference( Common.token_tb1);


        Token token = new Token( FirebaseInstanceId.getInstance().getToken() );

        tokens.child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                .setValue( token );

    }

    private void sendRequestToDriver(String driverId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tb1);

        tokens.orderByKey().equalTo( driverId )
                .addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot : dataSnapshot.getChildren())
                {
                    Token token = postSnapShot.getValue( Token.class );

                    String json_lat_lng = new Gson().toJson( new LatLng( mLastLocation.getLatitude(),mLastLocation.getLongitude() ));

                    String riderToken = FirebaseInstanceId.getInstance().getToken();
                    Notification data = new Notification( riderToken,json_lat_lng );

                    Sender content = new Sender( token.getToken(),data );

                    mService.sendMessage( content )
                            .enqueue( new Callback <FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1)
                                        Toasty.success(Home.this, "Request sent!", Toast.LENGTH_SHORT, true).show();
                                    else
                                        Toasty.error( Home.this, "Failed ! ", Toast.LENGTH_SHORT, true).show();


                                }

                                @Override
                                public void onFailure(Call <FCMResponse> call, Throwable t) {

                                    Log.e( "Error" , t.getMessage() );

                                }
                            } );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );
    }

    private void requestPickupHere(String uid) {

        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference( Common.pickup_request_tb1 );
        GeoFire mGeoFire = new GeoFire( dbRequest );

       mGeoFire.setLocation( uid,new GeoLocation( mLastLocation.getLatitude(),mLastLocation.getLongitude() ) );


        if(mUserMarker.isVisible())
            mUserMarker.remove();

        // Add marker

        mUserMarker = mMap.addMarker( new MarkerOptions()
        .title( "Pickup Here" )
        .snippet( "  " )
        .position( new LatLng( mLastLocation.getLatitude(),mLastLocation.getLongitude() ) )
        .icon( BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) ));

        mUserMarker.showInfoWindow();

        btnPickUpRequest.setText( "Getting Your Driver ... " );

        findDriver();
    }

    private void findDriver() {
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
        GeoFire gfDrivers = new GeoFire( drivers );
        final GeoQuery geoQuery = gfDrivers.queryAtLocation( new GeoLocation( mLastLocation.getLatitude(),mLastLocation.getLongitude() )
                            ,radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener( new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                // if Found

                if(!Common.isDriverFound)
                {
                    Common.isDriverFound = true;
                    Common.driverId = key ;
                    btnPickUpRequest.setText( " CALL DRIVER" );
                  //  Toasty.success(Home.this, " " + key, Toast.LENGTH_SHORT, true).show();


                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                // if still not found driver increase distance

                if(!Common.isDriverFound && radius<LIMIT)
                {
                    radius++;
                    findDriver();
                }
                else {
                    if (!Common.isDriverFound) {
                        Toasty.error( Home.this, " No Ride is Avaible near you.", Toast.LENGTH_SHORT, true ).show();
                        btnPickUpRequest.setText( "REQUEST PICKUP" );
                        geoQuery.removeAllListeners();
                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        } );


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {              case MY_PERMISSION_REQUEST_CODE:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if (checkPlayServices()) {
                    buildGoogleApiClient();
                    createLocationRequest();
                    displayLocation();


                }

            }
            break;

        }
    }

    private void setUpLoaction() {

        if (ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            //Request Runtime permission
            ActivityCompat.requestPermissions( this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE );
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();


            }
        }
    }

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION  )!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION   )!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient );
        if (mLastLocation != null)

        {
            // Create Latlng from mLastLocation and this is center point

            LatLng center = new LatLng( mLastLocation.getLatitude(),mLastLocation.getLongitude() );

            LatLng northSide = SphericalUtil.computeOffset( center,10000,0 );
            LatLng southSide = SphericalUtil.computeOffset( center,10000,180 );

            LatLngBounds bounds = LatLngBounds.builder()
                   .include( northSide )
                    .include( southSide )
                    .build();

            place_location.setBoundsBias( bounds );
            place_location.setFilter( typeFilter );

            place_destination.setBoundsBias( bounds );
            place_destination.setFilter( typeFilter );


            // presence system

            driverAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
            driverAvailable.addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // if we have any change on driver table then we will reload all avainable drivers

                    loadAllAvailableDrivers(new LatLng( mLastLocation.getLatitude(),mLastLocation.getLongitude() ));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );

                final double latitude = mLastLocation.getLatitude();
                final double longitude = mLastLocation.getLongitude();


                //update to firebase


                        // Add Marker

                        if(mUserMarker != null)
                            mUserMarker.remove();  // remove old marker

                        mUserMarker = mMap.addMarker( new MarkerOptions()
                                .position( new LatLng( latitude,longitude ) )
                                .title( String.format( "You" )  )
                        );

                        // Move Camera to this positon
                        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom( new LatLng( latitude,longitude ) ,15.5f) );
                        // Draw animation to rotate marker

                        rotateMarker(mUserMarker,360,mMap);

                        loadAllAvailableDrivers(new LatLng( mLastLocation.getLatitude(),mLastLocation.getLongitude() ) );

                Log.d("Location Change ",String.format( "Your Location was Changed :%f/%f",latitude,longitude ));

        }
        else {
            Log.d("Error","Cannot get your Location");
        }
    }

    private void loadAllAvailableDrivers(final LatLng location) {

        // first we nwwd to delete all markers on map (include our location marker and our available drivers marker)

        mMap.clear();

        // After that just add our location again

        mMap.addMarker( new MarkerOptions().position( location )
                        .title( "YOU" ));

        // load all drivers in distance of 3 km

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
        GeoFire gf = new GeoFire( driverLocation );

         GeoQuery geoQuery = gf.queryAtLocation( new GeoLocation( location.latitude,location.longitude ),distance );
         geoQuery.removeAllListeners();

         geoQuery.addGeoQueryEventListener( new GeoQueryEventListener() {
             @Override
             public void onKeyEntered(String key, final GeoLocation location) {

                 FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)
                         .child( key )
                         .addListenerForSingleValueEvent( new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                 Rider rider = dataSnapshot.getValue(Rider.class);

                                 // Add driver to map
                                 mMap.addMarker( new MarkerOptions()
                                 .position( new LatLng( location.latitude,location.longitude ) )
                                 .flat( true )
                                 .title("Driver : "+ rider.getName())
                                 .snippet( "Phone : "+ rider.getPhone() )
                                 .icon( BitmapDescriptorFactory.fromResource( R.drawable.driver )));
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                         } );

             }

             @Override
             public void onKeyExited(String key) {

             }

             @Override
             public void onKeyMoved(String key, GeoLocation location) {

             }

             @Override
             public void onGeoQueryReady() {

                 if(distance <= LIMIT) // distance just fifn for 3 km
                 {
                     distance++;
                     loadAllAvailableDrivers(location);
                 }
             }

             @Override
             public void onGeoQueryError(DatabaseError error) {

             }
         } );
    }

    private void rotateMarker( final Marker mUserMarker, final float i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mUserMarker.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post( new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation( (float) elapsed / duration );
                float rot = t * i + (1 - t) * startRotation;
                mUserMarker.setRotation( -rot > 180 ? rot / 2 : rot );

                if (t < 1.0) {
                    handler.postDelayed( this, 16 );
                }
            }
        } );
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval( UPDATE_INTERVAL );
        mLocationRequest.setFastestInterval( FASTEST_INTERVAL );
        mLocationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
        mLocationRequest.setSmallestDisplacement( DISPLACEMENT );
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this );

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError( resultCode ))
                GooglePlayServicesUtil.getErrorDialog( resultCode, this, PLAY_SERVICE_RES_REQUEST ).show();
            else {
                Toasty.error(Home.this, "This device is not supported ", Toast.LENGTH_SHORT, true).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        if (drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.home, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected( item );
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

      if (id == R.id.nav_signOut)
      {
          SignOut();
      }
        else if (id == R.id.nav_UpdateInformation)
        {
           showUpdateInforamtionDialog();
        }
        return true;
    }

    private void showUpdateInforamtionDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this );
        dialog.setTitle( "Update Inforamtion" );
        dialog.setMessage("Please fill all the Inforamtion");

        LayoutInflater inflater= LayoutInflater.from(this);

        View update_info_layout=inflater.inflate(R.layout.layout_update_information,null);



        final MaterialEditText edtName = update_info_layout.findViewById( R.id.edt_Name );
        final MaterialEditText edtPhone = update_info_layout.findViewById( R.id.edt_Phone );
        final ImageView imgAvatar = update_info_layout.findViewById( R.id.imageAvatar );

        dialog.setView( update_info_layout );

        imgAvatar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseImageandUpload();
            }
        } );

        dialog.setView( update_info_layout );
        dialog.setPositiveButton( "UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                final AlertDialog waitingDialog = new SpotsDialog( Home.this );
                  waitingDialog.show();
                  String name = edtName.getText().toString();
                  String phone = edtPhone.getText().toString();

                Map<String,Object> update = new HashMap <>(  );
                if (!TextUtils.isEmpty(name))
                    update.put( "name",name );
                if (!TextUtils.isEmpty(phone))
                    update.put( "phone",phone );

                //update

                DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tb1);
                riderInformation.child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                        .updateChildren( update ).addOnCompleteListener( new OnCompleteListener <Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        waitingDialog.dismiss();
                        if (task.isSuccessful())
                            Toasty.success( Home.this ,"Inforamtion Updated !",Toast.LENGTH_LONG,true).show();
                        else
                            Toasty.error( Home.this ,"Inforamtion wasn't Updated !",Toast.LENGTH_LONG,true).show();
                    }
                } );





            }
        } );

        dialog.setNegativeButton( "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        } );

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)

        {
            final Uri saveUri = data.getData();
            Toast.makeText( this,saveUri.toString(),Toast.LENGTH_LONG).show();
            if (saveUri != null)
            {
                final ProgressDialog progressDialog = new ProgressDialog( this );

                progressDialog.setMessage( "Uploading..." );
                progressDialog.show();

                String imageName = UUID.randomUUID().toString();

                final StorageReference imageFolder = storageReference.child( "images/"+imageName );

                imageFolder.putFile( saveUri )
                        .addOnSuccessListener( new OnSuccessListener <UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();

                           imageFolder.getDownloadUrl().addOnSuccessListener( new OnSuccessListener <Uri>() {
                               @Override
                               public void onSuccess(Uri uri) {
                                   Map<String,Object> update = new HashMap <>(  );

                                   update.put( "avatarUrl",uri.toString() );

                                   DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tb1);
                                   riderInformation.child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                                           .updateChildren( update ).addOnCompleteListener( new OnCompleteListener <Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()) {
                                               imageAvatar.setImageURI( saveUri );
                                               Toasty.success( Home.this, "Image Was Uploaded", Toast.LENGTH_LONG, true ).show();
                                           }
                                           else
                                               Toasty.error( Home.this ,"Image wasn't Updated !",Toast.LENGTH_LONG,true).show();
                                       }
                                   } ).addOnFailureListener( new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           Toasty.error( Home.this ,e.getMessage(),Toast.LENGTH_LONG,true).show();

                                       }
                                   } );
;                               }
                           } );

                            }
                        } ).addOnProgressListener( new OnProgressListener <UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                        progressDialog.setMessage( "Uploaded " +progress +" %" );
                    }
                } );
            }
        }


    }

    private void ChooseImageandUpload() {
        Intent intent = new Intent(  );
        intent.setType( "image/*" );
        intent.setAction( Intent.ACTION_GET_CONTENT );
        startActivityForResult( Intent.createChooser(  intent,"Select Picture"),Common.PICK_IMAGE_REQUEST );
    }



    private void SignOut() {

        // Reset Remeber VALue
        Paper.init( this );
        Paper.book().destroy();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent( Home.this,MainActivity.class );
        startActivity( intent );
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
       mMap = googleMap;
       mMap.getUiSettings().setZoomControlsEnabled( true );
       mMap.getUiSettings().setZoomGesturesEnabled( true );
       mMap.setInfoWindowAdapter( new CustomInfoWindow( this ) );

       mMap.setOnMapClickListener( new GoogleMap.OnMapClickListener() {
           @Override
           public void onMapClick(LatLng latLng) {



               if(markerDestination != null)
                   markerDestination.remove();

               markerDestination= mMap.addMarker( new MarkerOptions().icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_BLUE ) )
               .position( latLng )
               .title( "Destination" ));
               mMap.animateCamera( CameraUpdateFactory.newLatLngZoom( latLng,15.0f ) );
               BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance( String.format( "%f,%f",mLastLocation.getLatitude(),mLastLocation.getLongitude() ),
                       String.format( "%f,%f",latLng.latitude,latLng.longitude ),
                       true );
               Home.super.onPostResume();
               mBottomSheet.show( getSupportFragmentManager(),mBottomSheet.getTag());
           }
       } );
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if(ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION  )!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION   )!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient,mLocationRequest,this );

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }
}
