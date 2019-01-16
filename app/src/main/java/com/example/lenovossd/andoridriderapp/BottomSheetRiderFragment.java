package com.example.lenovossd.andoridriderapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovossd.andoridriderapp.Common.Common;
import com.example.lenovossd.andoridriderapp.Remote.IGoogleAPI;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {

    String mLocation,mDestination;

    boolean isTapOnMap;

    TextView txtCalculate,txtLocation,txtDestination;
    IGoogleAPI mService;

    public static BottomSheetRiderFragment newInstance(String location,String destination,boolean isTapOnMap)
    {
        BottomSheetRiderFragment f = new BottomSheetRiderFragment();
        Bundle args = new Bundle(  );
        args.putString("location",location );
        args.putString("destination",destination );
        args.putBoolean( "isTapOnMap" ,isTapOnMap);
        f.setArguments( args );
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
       mLocation = getArguments().getString( "location" );
       mDestination = getArguments().getString( "destination" );
        isTapOnMap  = getArguments().getBoolean( "isTapOnMap" );

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.bottom_sheet_rider ,container,false);

         txtLocation = (TextView)view.findViewById( R.id.txtLocation );
         txtDestination = (TextView)view.findViewById( R.id.txtDestination );
        txtCalculate = (TextView)view.findViewById( R.id.txtCalculate );
       // getPrice(mLocation,mDestination);
        mService = Common.getGoogleService();

        if (!isTapOnMap) {
            // control comes there  when no click on map
            txtLocation.setText( mLocation );
            txtDestination.setText( mDestination );
            LatLng Locationlatlng = convertAddress( mLocation );
            LatLng Destinationlatlng = convertAddress( mDestination );
            distancebetween( Locationlatlng, Destinationlatlng );
             }
             else
                 {
                     String parts[] = mLocation.split( "," );
                     double mLocLat = Double.parseDouble( parts[0] );
                     double mLocLng = Double.parseDouble( parts[1] );
                     String starting_address = customeraddress( mLocLat, mLocLng);
                     LatLng Locationlatlng =  convertAddress(starting_address  );
                     String parts2[] = mDestination.split( "," );

                     double mDesLat = Double.parseDouble( parts2[0] );
                     double mDesLng = Double.parseDouble( parts2[1] );
                     String end_address = customeraddress( mDesLat, mDesLng);
                     LatLng Destinationlatlng =  convertAddress(end_address  );
                     distancebetween( Locationlatlng, Destinationlatlng );
                     txtLocation.setText(starting_address);
                     txtDestination.setText(end_address);


                     }









        return view;
    }
    // calculate the distance between starting point and ending point

    private void distancebetween(LatLng locationlatlng, LatLng destinationlatlng) {


            Location driverA = new Location( "point A" );
            driverA.setLatitude( locationlatlng.latitude );
            driverA.setLongitude( locationlatlng.longitude );
            Location riderB = new Location( "point B" );
            riderB.setLatitude( destinationlatlng.latitude );
            riderB.setLongitude( destinationlatlng.longitude );
            double distance = driverA.distanceTo( riderB );
            double roundvalue = Math.round( distance );
            int final_distance = (int) roundvalue / 1000;
            int price = (int) Common.getPrice( final_distance );
            int secondPrice = price + 50;
            txtCalculate.setText( Integer.toString( price ) + " - " + Integer.toString( secondPrice ) + " PKR" );


        }
        // convert string adress in to lat lng
    private LatLng convertAddress(String Adress) {
        LatLng lng = null;
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(Adress, 1);
            if(addresses.size() > 0) {
                double latitude= addresses.get(0).getLatitude();
                double longitude= addresses.get(0).getLongitude();
                lng = new LatLng(latitude,longitude  );

                }
        } catch (IOException e) {
            e.printStackTrace();
        }
            return lng;
    }

    // convert the lat lng to string address

    private String customeraddress(double lat, double lng) {
        String myCity="";

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault() );
        try {
            List<Address> addresses = geocoder.getFromLocation( lat,lng,3);
            String address = addresses.get( 0 ).getAddressLine( 0 );
            myCity = address;


            Log.d("complete address","Address : " +address.toString() );

        } catch (IOException e) {
            e.printStackTrace();
        }
        return myCity;
    }


    private void getPrice(String mLocation, String mDestination) {
        // testing version

        String requestUrl = null;
        try{
            requestUrl =  "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ mLocation+"&"+
                    "destination="+mDestination+"&"+
                    "key="+getResources().getString( R.string.google_browser_key);
            Log.e("Link",requestUrl);
            mService.getPath( requestUrl ).enqueue( new Callback <String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject( response.body().toString());

                        JSONArray routes = jsonObject.getJSONArray( "routes" );

                        JSONObject object = routes.getJSONObject( 0 );
                        JSONArray legs = object.getJSONArray( "legs" );

                        JSONObject legsObject = legs.getJSONObject( 0);

                        // get distance

                        JSONObject distance = legsObject .getJSONObject( "distance" );
                        String distance_text = distance.getString( "text" );
                        Double distance_value = Double.parseDouble( distance_text.replaceAll( "[^0-9\\\\.]+","" ) );

                        JSONObject time = legsObject.getJSONObject("duration" );

                        String time_text = time.getString( "text" );

                        Integer time_value = Integer.parseInt( time_text.replaceAll( "\\D+","" ) );

                        //String final_calculate = String.format(Locale.getDefault(), "%s + %s = $%.2f",distance_text,time_text ,
                                                   // Common.getPrice( distance_value,time_value ));
                        String final_calculate = String.format( "$",Common.getPrice( distance_value ));

                        txtCalculate.setText( final_calculate );

                            if (isTapOnMap)
                            {
                                String start_address = legsObject.getString("start_location" );
                                String end_address = legsObject.getString( "end_location" );

                                txtLocation.setText( start_address );
                                txtDestination.setText( end_address );
                            }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call <String> call, Throwable t) {
                    Log.e( "Error",t.getMessage() );
                }
            } );
        }
        catch (Exception ex )
        {
            ex.printStackTrace();
        }






    }
}
