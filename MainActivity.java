 package com.example.wonderclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private View panelAi, panelYt;
    private final List<String[]> chat = new ArrayList<String[]>();
    private final List<String[]> videos = new ArrayList<String[]>(); // id,title,sub
    private BaseAdapter chatAdapter, videoAdapter;
    private EditText chatInput, ytInput;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        panelAi = findViewById(R.id.panelAi);
        panelYt = findViewById(R.id.panelYt);
        chatInput = (EditText) findViewById(R.id.chatInput);
        ytInput = (EditText) findViewById(R.id.ytInput);

        findViewById(R.id.btnAi).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { panelAi.setVisibility(View.VISIBLE); panelYt.setVisibility(View.GONE); }
        });
        findViewById(R.id.btnYt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { panelAi.setVisibility(View.GONE); panelYt.setVisibility(View.VISIBLE); }
        });

        chatAdapter = new BaseAdapter() {
            public int getCount() { return chat.size(); }
            public Object getItem(int i) { return chat.get(i); }
            public long getItemId(int i) { return i; }
            public View getView(int i, View cv, ViewGroup p) {
                if (cv == null) cv = getLayoutInflater().inflate(R.layout.item_chat, null);
                TextView t = (TextView) cv.findViewById(R.id.msg);
                String[] m = chat.get(i);
                boolean me = m[0].equals("user");
                t.setText(m[1]);
                t.setGravity(me ? Gravity.RIGHT : Gravity.LEFT);
                t.setBackgroundColor(me ? Color.parseColor("#1B5E20") : Color.parseColor("#263238"));
                return cv;
            }
        };
        ((ListView) findViewById(R.id.chatList)).setAdapter(chatAdapter);
        findViewById(R.id.chatSend).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { sendChat(); }
        });

        videoAdapter = new BaseAdapter() {
            public int getCount() { return videos.size(); }
            public Object getItem(int i) { return videos.get(i); }
            public long getItemId(int i) { return i; }
            public View getView(int i, View cv, ViewGroup p) {
                if (cv == null) cv = getLayoutInflater().inflate(R.layout.item_video, null);
                String[] v = videos.get(i);
                ((TextView) cv.findViewById(R.id.title)).setText(v[1]);
                ((TextView) cv.findViewById(R.id.sub)).setText(v[2]);
                ImageView iv = (ImageView) cv.findViewById(R.id.thumb);
                iv.setImageBitmap(null);
                iv.setTag(v[0]);
                new ThumbTask(iv, v[0]).execute();
                return cv;
            }
        };
        ListView yl = (ListView) findViewById(R.id.ytList);
        yl.setAdapter(videoAdapter);
        yl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                Intent i = new Intent(MainActivity.this, PlayerActivity.class);
                i.putExtra("id", videos.get(pos)[0]);
                startActivity(i);
            }
        });
        findViewById(R.id.ytSearch).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { search(); }
        });
    }

    private void sendChat() {
        final String text = chatInput.getText().toString().trim();
        if (text.length() == 0) return;
        chatInput.setText("");
        chat.add(new String[]{"user", text});
        chatAdapter.notifyDataSetChanged();
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... a) {
                try {
                    JSONObject o = new JSONObject();
                    o.put("message", text);
                    JSONObject r = new JSONObject(Api.post(MainActivity.this, "/chat", o.toString()));
                    return r.has("reply") ? r.getString("reply") : "Hata: " + r.optString("error");
                } catch (Exception e) {
                    return "Bağlantı hatası: " + e.getMessage();
                }
            }
            protected void onPostExecute(String r) {
                chat.add(new String[]{"model", r});
                chatAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private void search() {
        final String q = ytInput.getText().toString().trim();
        if (q.length() == 0) return;
        ((android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(ytInput.getWindowToken(), 0);
        Toast.makeText(this, "Aranıyor...", Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, List<String[]>>() {
            String err;
            protected List<String[]> doInBackground(Void... a) {
                List<String[]> out = new ArrayList<String[]>();
                try {
                    String enc = java.net.URLEncoder.encode(q, "UTF-8");
                    JSONObject r = new JSONObject(Api.get(MainActivity.this, "/search?q=" + enc));
                    if (r.has("error")) { err = r.getString("error"); return out; }
                    JSONArray arr = r.getJSONArray("results");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        out.add(new String[]{o.getString("id"), o.getString("title"),
                                o.optString("channel") + "  •  " + o.optString("duration")});
                    }
                } catch (Exception e) { err = e.getMessage(); }
                return out;
            }
            protected void onPostExecute(List<String[]> r) {
                if (err != null) Toast.makeText(MainActivity.this, "Hata: " + err, Toast.LENGTH_LONG).show();
                videos.clear();
                videos.addAll(r);
                videoAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private class ThumbTask extends AsyncTask<Void, Void, Bitmap> {
        final ImageView iv; final String id;
        ThumbTask(ImageView iv, String id) { this.iv = iv; this.id = id; }
        protected Bitmap doInBackground(Void... a) {
            try {
                byte[] d = Api.getBytes(MainActivity.this, "/thumb?id=" + id);
                return BitmapFactory.decodeByteArray(d, 0, d.length);
            } catch (Exception e) { return null; }
        }
        protected void onPostExecute(Bitmap b) {
            if (b != null && id.equals(iv.getTag())) iv.setImageBitmap(b);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.main_menu, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem it) {
        if (it.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (it.getItemId() == R.id.menu_reset) {
            chat.clear();
            chatAdapter.notifyDataSetChanged();
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... a) {
                    try { Api.get(MainActivity.this, "/reset"); } catch (Exception e) {}
                    return null;
                }
            }.execute();
        }
        return true;
    }
}
