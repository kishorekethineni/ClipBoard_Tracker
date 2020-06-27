package com.kishorekethineni.clipboardmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class WebView_ClipBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        WebView browser = (WebView) findViewById(R.id.webview);
        Intent i=getIntent();
        String Copied_URL=i.getStringExtra("URL");
        browser.loadUrl(Copied_URL);
    }
}
