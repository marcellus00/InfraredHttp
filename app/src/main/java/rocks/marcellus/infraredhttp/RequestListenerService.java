package rocks.marcellus.infraredhttp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.obd.infrared.InfraRed;
import com.obd.infrared.log.LogToAir;
import com.obd.infrared.log.LogToConsole;
import com.obd.infrared.log.Logger;
import com.obd.infrared.patterns.PatternAdapter;
import com.obd.infrared.patterns.PatternConverter;
import com.obd.infrared.patterns.PatternConverterUtils;
import com.obd.infrared.patterns.PatternType;
import com.obd.infrared.transmit.TransmitInfo;
import com.obd.infrared.transmit.TransmitterType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static rocks.marcellus.infraredhttp.R.drawable.ic_launcher_foreground;

public class RequestListenerService extends Service implements WebServer.ResponseListener {

    private WebServer _server;
    private InfraRed _infraRed;
    private TransmitterType _transmitterType;
    private PatternAdapter _patternAdapter;

    public RequestListenerService() {
    }

    @Override
    public void onCreate() {

        InitHardware();
        InitServer();
        InitService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _server.RemoveListener(this);
        _server.stop();
        _infraRed.stop();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void OnResponse(String response) {
        BeamCommand(response);
    }

    private void BeamCommand(String command)
    {
        List<PatternConverter> rawPatterns = new ArrayList<>();
        rawPatterns.add(PatternConverterUtils.fromString(PatternType.Intervals, 38000, command));

        TransmitInfo[] transmitInfoArray = new TransmitInfo[rawPatterns.size()];
        for (int i = 0; i < transmitInfoArray.length; i++) {
            transmitInfoArray[i] = _patternAdapter.createTransmitInfo(rawPatterns.get(i));
        }

        for (TransmitInfo transmitInfo : transmitInfoArray) {
            _infraRed.transmit(transmitInfo);
        }
    }

    private void InitHardware() {
        Logger log = new LogToAir("IR");
        _infraRed = new InfraRed(this.getApplication(), log);
        _transmitterType = _infraRed.detect();
        _patternAdapter = new PatternAdapter(log, _transmitterType);

        _infraRed.createTransmitter(_transmitterType);
    }

    private void InitServer() {
        _server = new WebServer(8080);
        _server.AddListener(this);
        try {
            _server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void InitService() {
        Intent notificationIntent = new Intent(this, InfoActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(ic_launcher_foreground)
                .setContentText("Receiving requests")
                .setContentTitle("IR over HTTP")
                .setTicker("Server is running")
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);
    }
}
