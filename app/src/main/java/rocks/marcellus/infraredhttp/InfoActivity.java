package rocks.marcellus.infraredhttp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class InfoActivity extends AppCompatActivity {

    private Intent _intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        _intent = new Intent(InfoActivity.this, RequestListenerService.class);
        startService(_intent);
    }

    public void onClick(android.view.View view) {
        stopService(_intent);
        finishAndRemoveTask();
    }
}
