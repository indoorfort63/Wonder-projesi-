package com.example.wonderclient;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Api {
    public static String base(Context c) {
        SharedPreferences p = c.getSharedPreferences("cfg", 0);
        return "http://" + p.getString("ip", "192.168.1.10") + ":" + p.getString("port", "8080");
    }

    public static String get(Context c, String path) throws IOException {
        return request(base(c) + path, null);
    }

    public static String post(Context c, String path, String json) throws IOException {
        return request(base(c) + path, json);
    }

    public static byte[] getBytes(Context c, String path) throws IOException {
        HttpURLConnection h = (HttpURLConnection) new URL(base(c) + path).openConnection();
        h.setConnectTimeout(8000);
        h.setReadTimeout(20000);
        return readAll(h.getInputStream());
    }

    private static String request(String u, String body) throws IOException {
        HttpURLConnection h = (HttpURLConnection) new URL(u).openConnection();
        h.setConnectTimeout(8000);
        h.setReadTimeout(90000);
        if (body != null) {
            h.setRequestMethod("POST");
            h.setDoOutput(true);
            h.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            OutputStream o = h.getOutputStream();
            o.write(body.getBytes("UTF-8"));
            o.close();
        }
        InputStream in = h.getResponseCode() >= 400 ? h.getErrorStream() : h.getInputStream();
        if (in == null) return "{}";
        return new String(readAll(in), "UTF-8");
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
        in.close();
        return b.toByteArray();
    }
}
