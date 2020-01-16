package org.tensorflow.lite.examples.posenet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.tensorflow.lite.examples.posenet.Adapter.RecyclerViewAdapter;
import org.tensorflow.lite.examples.posenet.AlarmDB.AlarmInfo;
import org.tensorflow.lite.examples.posenet.Utils.Codes;
import org.tensorflow.lite.examples.posenet.ViewModel.AlarmViewModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import android.bluetooth.BluetoothAdapter;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.OnListItemSelectedInterface{

    FloatingActionButton addAlarmBtn;
    Intent intent;

    private AlarmViewModel alarmViewModel;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    private ArrayList<AlarmInfo> list;
    private BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        addAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, AlarmSettingActivity.class);
                AlarmInfo alarmInfo = new AlarmInfo();
                intent.putExtra("flag", 2);
                intent.putExtra("origin", alarmInfo);
                startActivityForResult(intent, Codes.TIME_SETTING_REQUEST_CODE);
            }
        });
        //블
        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
                setup();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        //연결시도
        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            bt.disconnect();
        } else {
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }


    }
    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리

            }
        }
    }

    public void setup() {
        //데이터 전송

        bt.send("e", true);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Codes.NEW_ALARM_CODE){
            AlarmInfo alarmInfo = (AlarmInfo) data.getSerializableExtra("alarm_info");
            list.add(alarmInfo);
            alarmViewModel.insert(alarmInfo);
//            Toast.makeText(this, alarmInfo.getHour(), Toast.LENGTH_SHORT).show();
        }
        else if(resultCode == Codes.DELETE_THIS_ALARM){
            AlarmInfo alarmInfo = (AlarmInfo) data.getSerializableExtra("delete_this");
            list.remove(alarmInfo);
            alarmViewModel.delete(alarmInfo);
        }
//블

        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void init(){

        addAlarmBtn = findViewById(R.id.fab_main);

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new RecyclerViewAdapter(this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        alarmViewModel = ViewModelProviders.of(this).get(AlarmViewModel.class);
        alarmViewModel.getAllAlarmInfo().observe(this, new Observer<List<AlarmInfo>>() {
            @Override
            public void onChanged(List<AlarmInfo> alarmInfos) {

                adapter.setAlarms(alarmInfos);

                list = new ArrayList<>();

                for(int i=0; i<alarmInfos.size(); i++){
                    list.add(alarmInfos.get(i));
                }
            }
        });
    }

    @Override
    public void onItemSelected(View v, int pos) {
        RecyclerViewAdapter.ItemViewHolder viewHolder = (RecyclerViewAdapter.ItemViewHolder)recyclerView.findViewHolderForAdapterPosition(pos);
    }
}
