package com.geekoders.smartbin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainPageActivity extends AppCompatActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Bin Details");
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new BinDetailsFragment()).addToBackStack(null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void findOptimalRoute(View v) {
        toolbar.setTitle("Optimal Route");
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new GoogleMapViewFragment()).addToBackStack(null).commit();
    }

    public void giveBinDetails(View v) {
        toolbar.setTitle("Bin Details");
        Toast.makeText(getApplicationContext(),
                "You clicked on Bin Details", Toast.LENGTH_SHORT)
                .show();
    }

    public void giveBinAnalytics(View v) {
        toolbar.setTitle("Bin Analytics");
        Toast.makeText(getApplicationContext(),
                "You clicked on Bin Analytics", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.action_dashboard:
                Toast.makeText(getBaseContext(), "You selected Dashboard", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_export:
                Toast.makeText(getBaseContext(), "You selected Export", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_password_lock:
                Toast.makeText(getBaseContext(), "You selected Password Lock", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_settings:
                Toast.makeText(getBaseContext(), "You selected Settings", Toast.LENGTH_SHORT).show();
                break;

        }
        return true;
    }
}
