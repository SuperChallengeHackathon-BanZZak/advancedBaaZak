package org.tensorflow.lite.examples.posenet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.OnListItemSelectedInterface{

    FloatingActionButton addAlarmBtn;
    Intent intent;

    private AlarmViewModel alarmViewModel;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    private ArrayList<AlarmInfo> list;

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