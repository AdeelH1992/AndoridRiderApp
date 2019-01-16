package com.example.lenovossd.andoridriderapp.Common;

import com.example.lenovossd.andoridriderapp.Model.Rider;
import com.example.lenovossd.andoridriderapp.Remote.FCMClient;
import com.example.lenovossd.andoridriderapp.Remote.GoogleMapAPI;
import com.example.lenovossd.andoridriderapp.Remote.IFCMService;
import com.example.lenovossd.andoridriderapp.Remote.IGoogleAPI;

public class Common {

    public static final int PICK_IMAGE_REQUEST =9999 ;
    public static boolean  isDriverFound = false;
   public static String driverId = "";
    public static final String driver_tb1 = "Drivers";
    public static final String user_driver_tb1 = "DriversInformation";
    public static final String user_rider_tb1 = "RidersInformation";
    public static final String pickup_request_tb1 = "PickupRequets";
    public static final String token_tb1 = "Tokens";
    public static final String rate_detail_tb1 = "RateDetails";


    public static double base_fare=100;
    public static double time_rate=4.67;
    public static double distance_rate=12;


    public static final String fcmURL = "https://fcm.googleapis.com";
    public static final String googleAPIUrl = "https://maps.googleapis.com";
     public static final String user_field ="rider_usr";
     public static final String pwd_field ="rider_pwd";

    public static Rider currentUser = new Rider(  ) ;


    public static  double getPrice (double km )
    {
        return (base_fare+(distance_rate*km));

    }

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient( fcmURL ).create(IFCMService .class );


    }
    public static IGoogleAPI getGoogleService()
    {
        return GoogleMapAPI.getClient( googleAPIUrl ).create(IGoogleAPI .class );


    }


}
