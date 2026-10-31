package com.example.wonderclient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_settings);
        final SharedPreferences p = getSharedPreferences("cfg", 0);
        final EditText ip = (EditText) findViewById(R.id.setIp);
        final EditText port = (EditText) findViewById(R.id.setPort);
        final TextView status = (TextView) findViewById(R.id.setStatus);
        ip.setText(p.getString("ip", "192.168.1.10"));
        port.setText(p.getString("port", "8080"));

        findViewById(R.id.setSave).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                p.edit().putString("ip", ip.getText().toString().trim())
                        .putString("port", port.getText().toString().trim()).commit();
                Toast.makeText(SettingsActivity.this, "Kaydedildi", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.setTest).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                p.edit().putString("ip", ip.getText().toString().trim())
                        .putString("port", port.getText().toString().trim()).commit();
                status.setText("Test ediliyor...");
                new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... a) {
                        try {
                            return "Bağlandı: " + Api.get(SettingsActivity.this, "/ping");
                        } catch (Exception e) {
                            return "Hata: " + e;
                        }
                    }
                    protected void onPostExecute(String r) { status.setText(r); }
                }.execute();
            }
        });
    }
}
