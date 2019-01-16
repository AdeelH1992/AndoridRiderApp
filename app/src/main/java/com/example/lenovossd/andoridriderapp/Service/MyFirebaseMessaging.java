package com.example.lenovossd.andoridriderapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.lenovossd.andoridriderapp.R;
import com.example.lenovossd.andoridriderapp.RateActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import es.dmoral.toasty.Toasty;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    public void onMessageReceived(final RemoteMessage remoteMessage) {

      if(remoteMessage.getNotification().getTitle().equals( "Cancel" ))
      {
          Handler handler = new Handler( Looper.getMainLooper() );

          handler.post( new Runnable() {
              @Override
              public void run() {
                  Toasty.info(MyFirebaseMessaging.this, ""+remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT, true).show();
              }
          } );
      }
      else if(remoteMessage.getNotification().getTitle().equals( "OnTheWay" ))
      {
          showOntheWayNotification(remoteMessage.getNotification().getBody());

      }

        else if(remoteMessage.getNotification().getTitle().equals( "Arrived" ))
        {
            showArrivedNotification(remoteMessage.getNotification().getBody());

        }


      else if(remoteMessage.getNotification().getTitle().equals( "DropOff" ))
      {
          openRateActivity(remoteMessage.getNotification().getBody());

      }


    }

    private void openRateActivity(String body) {

        Intent intent = new Intent( this, RateActivity.class);
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );

    }

    private void showArrivedNotification(String body) {


        PendingIntent contentIntent = PendingIntent.getActivity( getBaseContext(),
                0,new Intent(  ),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder( getBaseContext() );

        builder.setAutoCancel( true )
                .setDefaults( Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE|Notification.DEFAULT_SOUND )
                .setWhen( System.currentTimeMillis() )
                .setSmallIcon( R.mipmap.ic_launcher )
                .setContentTitle( "Arrived" )
                .setContentText( body )
                .setContentIntent( contentIntent );

        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService( Context.NOTIFICATION_SERVICE );
        manager.notify(1,builder.build());
    }
    private void showOntheWayNotification(String body) {


        PendingIntent contentIntent = PendingIntent.getActivity( getBaseContext(),
                0,new Intent(  ),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder( getBaseContext() );

        builder.setAutoCancel( true )
                .setDefaults( Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE|Notification.DEFAULT_SOUND )
                .setWhen( System.currentTimeMillis() )
                .setSmallIcon( R.mipmap.ic_launcher)
                .setContentTitle( "On His Way" )
                .setContentText( body )
                .setContentIntent( contentIntent );

        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService( Context.NOTIFICATION_SERVICE );
        manager.notify(1,builder.build());
    }
}
