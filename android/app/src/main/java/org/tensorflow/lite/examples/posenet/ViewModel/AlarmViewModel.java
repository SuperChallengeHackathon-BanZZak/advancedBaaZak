package  org.tensorflow.lite.examples.posenet.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.tensorflow.lite.examples.posenet.AlarmDB.AlarmInfo;
import org.tensorflow.lite.examples.posenet.AppDB.AppRepository;

import java.util.List;

public class AlarmViewModel extends AndroidViewModel {

    private final AppRepository repository;
    private final LiveData<List<AlarmInfo>> allAlarmInfo;

    public AlarmViewModel(Application application){
        super(application);
        repository = new AppRepository(application);
        allAlarmInfo = repository.getAllAlarmInfo();
    }

    public void insert(AlarmInfo alarmInfo) {repository.insert(alarmInfo);}
    public void delete(AlarmInfo alarmInfo) {repository.delete(alarmInfo);}
    public void deleteAll() {repository.deleteAll();}
    public LiveData<List<AlarmInfo>> getAllAlarmInfo() {return allAlarmInfo;}
}
