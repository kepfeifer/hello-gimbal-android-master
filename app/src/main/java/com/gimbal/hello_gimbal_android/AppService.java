package com.gimbal.hello_gimbal_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gimbal.android.Communication;
import com.gimbal.android.CommunicationListener;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Push;
import com.gimbal.android.Visit;

import java.util.LinkedList;
import java.util.List;

;


public class AppService extends Service {

    private PlaceEventListener placeEventListener;
    private CommunicationListener communicationListener;
    public static final String APPSERVICE_STARTED_ACTION = "appservice_started";
    private static final int MAX_NUM_EVENTS = 100;
    private LinkedList<String> events;

    SensorManager mSensor = (SensorManager)this.getSystemService(SENSOR_SERVICE);

    Sensor steps = mSensor.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);





    @Override
    public void onCreate(){




        events = new LinkedList<>(GimbalDAO.getEvents(getApplicationContext()));
        Gimbal.setApiKey(this.getApplication(), "9e186f5b-7941-456b-af3f-d0cccc660888");

        // Setup PlaceEventListener
        placeEventListener = new PlaceEventListener() {

            @Override
            public void onVisitStart(Visit visit) {
                //display the notification, picture thing
                addEvent(String.format("Start Visit for %s", visit.getPlace().getName()));

            }

            @Override
            public void onVisitEnd(Visit visit) {
                addEvent(String.format("End Visit for %s", visit.getPlace().getName()));
            }
        };
        PlaceManager.getInstance().addListener(placeEventListener);
        PlaceManager.getInstance().startMonitoring();

        // Setup CommunicationListener



        communicationListener = new CommunicationListener() {



            @Override
            public Notification.Builder prepareCommunicationForDisplay(Communication communication, Visit visit, int notificationId) {
                addEvent(String.format( "Communication Delivered :"+communication.getTitle()));



                // If you want a custom notification create and return it here
                Notification.Builder mBuilder =
                        new Notification.Builder(AppService.this)
                                .setSmallIcon(R.drawable.safe_step_icon)
                                .setContentTitle("Look Up")
                                .setContentText("You are about to cross the street, LOOK UP!");
                mBuilder.setPriority(Notification.PRIORITY_MAX);
                mBuilder.setColor(Color.GRAY);
                mBuilder.setLights(Color.CYAN, 4, 1);
                mBuilder.setVibrate(new long[]{1000, 1000});
                Uri alarmSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mBuilder.setSound(alarmSound);
                mBuilder.setTicker("LOOK UP!");
                //Notification.InboxStyle inboxStyle= new Notification.InboxStyle();
                // mBuilder.setStyle(inboxStyle);


                notificationId = 1;
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(notificationId, mBuilder.build());


                return mBuilder;
            }



            @Override
            public Notification.Builder prepareCommunicationForDisplay(Communication communication, Push push, int notificationId) {
                addEvent(String.format( "Push Communication Delivered :"+communication.getTitle()));
                // If you want a custom notification create and return it here
                return null;
            }

            @Override
            public void onNotificationClicked(List<Communication> communications) {
                for (Communication communication : communications) {
                    if(communication != null) {
                        addEvent(String.format( "Communication Clicked"));


                    }
                }
            }
        };
        CommunicationManager.getInstance().addListener(communicationListener);
        CommunicationManager.getInstance().startReceivingCommunications();

    }

    private void addEvent(String event) {
        while (events.size() >= MAX_NUM_EVENTS) {
            events.removeLast();
        }
        events.add(0, event);
        GimbalDAO.setEvents(getApplicationContext(), events);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        notifyServiceStarted();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        PlaceManager.getInstance().removeListener(placeEventListener);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyServiceStarted() {
        Intent intent = new Intent(APPSERVICE_STARTED_ACTION);
        sendBroadcast(intent);
    }
}
