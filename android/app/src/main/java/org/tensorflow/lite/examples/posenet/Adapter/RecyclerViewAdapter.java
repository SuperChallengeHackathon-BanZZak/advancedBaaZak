
package org.tensorflow.lite.examples.posenet.Adapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.posenet.AlarmDB.AlarmInfo;
import org.tensorflow.lite.examples.posenet.AlarmSettingActivity;
import org.tensorflow.lite.examples.posenet.CameraActivity;
import org.tensorflow.lite.examples.posenet.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ItemViewHolder> {

    Context context;
    private final LayoutInflater layoutInflater;
    private List<AlarmInfo> list = new ArrayList<>();
    private OnListItemSelectedInterface mListener;
    private OnItemClickListener itemListener = null;

    final int MODIFY_REQUEST_CODE = 2;

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.alarm_item, parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {

        holder.onBind(list.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = v.getContext();
//                Intent intent = new Intent(context, CameraActivity.class);
                Intent intent = new Intent(context, AlarmSettingActivity.class);

                intent.putExtra("origin", list.get(position));
                intent.putExtra("flag", 1);
                ((Activity) context).startActivityForResult(intent, MODIFY_REQUEST_CODE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public RecyclerViewAdapter(Context context, OnListItemSelectedInterface onListItemSelectedInterface){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        mListener = onListItemSelectedInterface;
    }

    public interface OnListItemSelectedInterface {
        void onItemSelected(View v, int pos);
    }

    public interface OnItemClickListener{
        void onItemClick(View v, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.itemListener = listener;
    }

    public void setAlarms(List<AlarmInfo> alarms){
        this.list = alarms;
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView tv1, tv2;

        public ItemViewHolder(View itemView){
            super(itemView);
            tv1 = itemView.findViewById(R.id.tv1);
            tv2 = itemView.findViewById(R.id.tv2);
        }

        void onBind(AlarmInfo alarmInfo){
            tv1.setText(alarmInfo.getHour() + " : " + alarmInfo.getMin());
            tv2.setText(alarmInfo.getAmpm());
        }
    }
}