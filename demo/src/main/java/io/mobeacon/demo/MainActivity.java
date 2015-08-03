package io.mobeacon.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import io.mobeacon.sdk.MobeaconSDK;

public class MainActivity extends AppCompatActivity {
    public static final String MOBEACON_APP_KEY = "550e8400-e29b-41d4-a716-446655440000";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.mobeacon.demo.R.layout.activity_main);

        //INIT Mobeacon SDK
        MobeaconSDK.getInstance(this.getBaseContext(), MOBEACON_APP_KEY, "DEBUG");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(io.mobeacon.demo.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == io.mobeacon.demo.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
