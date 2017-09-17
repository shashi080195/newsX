package com.example.shashi.hackerearth;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class BrowserView extends AppCompatActivity {

    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_view);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        WebView webView = (WebView)findViewById(R.id.webView);
        webView.setWebChromeClient(new WebViewChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    WebView webView = (WebView) v;
                    switch(keyCode)
                    {
                        case KeyEvent.KEYCODE_BACK:
                            if(webView.canGoBack())
                            {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(getIntent().getExtras().getString("url"));

    }
    public class WebViewChromeClient extends android.webkit.WebChromeClient
    {
        @Override
        public void onProgressChanged(WebView view, int progress) {

            progressBar.setProgress(progress);
            if(progress == 100) {
                progressBar.setVisibility(View.GONE);
            }
        }

    }
    public class WebViewClient extends android.webkit.WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
        }

    }

}
