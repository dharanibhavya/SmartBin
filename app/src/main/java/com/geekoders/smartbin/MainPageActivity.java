package com.geekoders.smartbin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new BinDetailsFragment()).addToBackStack(null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
        return true;
    }

    public void findOptimalRoute(View v) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new GoogleMapViewFragment()).addToBackStack(null).commit();
    }

    public void giveBinDetails(View v) {
        Toast.makeText(getApplicationContext(),
                "You clicked on Bin Details", Toast.LENGTH_SHORT)
                .show();
    }

    public void giveBinAnalytics(View v) {
        Toast.makeText(getApplicationContext(),
                "You clicked on Bin Analytics", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
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
