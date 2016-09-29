package com.gimbal.hello_gimbal_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;



public class MainActivity extends AppCompatActivity {
    private GimbalEventReceiver gimbalEventReceiver;
    private GimbalEventListAdapter adapter;
    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        startService(new Intent(this, AppService.class));


        adapter = new GimbalEventListAdapter(this);

        ListView listView = (ListView)findViewById(R.id.listview);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_settings:
                Intent i= new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class PlaceholderFragment extends Fragment{
        public PlaceholderFragment(){

        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            View rootView= inflater.inflate(R.layout.activity_main, container, false);
            return rootView;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();


        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        }

        gimbalEventReceiver = new GimbalEventReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GimbalDAO.GIMBAL_NEW_EVENT_ACTION);
        intentFilter.addAction(AppService.APPSERVICE_STARTED_ACTION);
        registerReceiver(gimbalEventReceiver, intentFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data)
    {
        if(requestCode == 1)
        {
            if(resultCode == RESULT_CANCELED)
            {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gimbalEventReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class GimbalEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().compareTo(GimbalDAO.GIMBAL_NEW_EVENT_ACTION) == 0) {
                    adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
                }
            }
        }
    }


}