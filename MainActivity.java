package com.teachernelly.younglearners;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String ACADEMY_URL = "https://teachernellyacademi.netlify.app/";
    private static final int FILE_CHOOSER_REQUEST = 1001;

    private WebView webView;
    private ProgressBar progressBar;
    private TextView offlineBanner;
    private ValueCallback<Uri[]> filePathCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFFFFFFFF);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        root.addView(main, new FrameLayout.LayoutParams(-1, -1));

        // Native app header: an app-specific feature outside the website WebView.
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(18, 10, 12, 10);
        header.setBackgroundColor(0xFF5B18D6);

        TextView title = new TextView(this);
        title.setText("🎓 Teacher Nelly Academy");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(16);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTypeface(null, 1);
        header.addView(title, new LinearLayout.LayoutParams(0, 56, 1));

        Button share = nativeButton("Share");
        header.addView(share, new LinearLayout.LayoutParams(-2, 52));
        share.setOnClickListener(v -> shareAcademy());
        main.addView(header);

        offlineBanner = new TextView(this);
        offlineBanner.setText("You're offline. Some academy features may not work.");
        offlineBanner.setTextColor(0xFF7A4B00);
        offlineBanner.setBackgroundColor(0xFFFFF0C2);
        offlineBanner.setPadding(16, 8, 16, 8);
        offlineBanner.setVisibility(View.GONE);
        main.addView(offlineBanner);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        main.addView(progressBar, new LinearLayout.LayoutParams(-1, 4));

        webView = new WebView(this);
        main.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));

        // Native bottom action bar: refresh, academy home, downloads and Android settings.
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        actions.setPadding(4, 5, 4, 5);
        actions.setBackgroundColor(0xFFF7F4FB);

        Button home = nativeButton("Home");
        Button refresh = nativeButton("Refresh");
        Button downloads = nativeButton("Downloads");
        Button settings = nativeButton("App info");
        actions.addView(home, new LinearLayout.LayoutParams(0, 50, 1));
        actions.addView(refresh, new LinearLayout.LayoutParams(0, 50, 1));
        actions.addView(downloads, new LinearLayout.LayoutParams(0, 50, 1));
        actions.addView(settings, new LinearLayout.LayoutParams(0, 50, 1));
        main.addView(actions);

        home.setOnClickListener(v -> webView.loadUrl(ACADEMY_URL));
        refresh.setOnClickListener(v -> webView.reload());
        downloads.setOnClickListener(v -> openDownloads());
        settings.setOnClickListener(v -> openAppInfo());

        setContentView(root);
        configureWebView();
        updateConnectivityBanner();

        if (savedInstanceState == null) {
            webView.loadUrl(ACADEMY_URL);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private Button nativeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setPadding(4, 0, 4, 0);
        return b;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);
        s.setUserAgentString(s.getUserAgentString() + " TeacherNellyAcademyAndroid/2.0");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
                progressBar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "File picker is unavailable.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                String cookies = CookieManager.getInstance().getCookie(url);
                if (cookies != null) request.addRequestHeader("Cookie", cookies);
                request.setTitle("Teacher Nelly Academy download");
                request.setDescription("Downloading academy resource...");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        "TeacherNellyAcademy_" + System.currentTimeMillis());
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    Toast.makeText(this, "Download started. Check Downloads.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Unable to download this file.", Toast.LENGTH_SHORT).show();
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                updateConnectivityBanner();
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                updateConnectivityBanner();
                Toast.makeText(MainActivity.this, "Could not load this page. Check your internet connection.", Toast.LENGTH_SHORT).show();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
    }

    private void updateConnectivityBanner() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = false;
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            connected = info != null && info.isConnected();
        }
        offlineBanner.setVisibility(connected ? View.GONE : View.VISIBLE);
    }

    private void shareAcademy() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Teacher Nelly's Young Learners Academy");
        send.putExtra(Intent.EXTRA_TEXT,
                "Learn • Practise • Play • Grow 🌟\n\nTeacher Nelly's Young Learners Academy:\n" + ACADEMY_URL);
        startActivity(Intent.createChooser(send, "Share Teacher Nelly Academy"));
    }

    private void openDownloads() {
        try {
            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Open your phone's Downloads folder to see saved resources.", Toast.LENGTH_LONG).show();
        }
    }

    private void openAppInfo() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception ignored) { }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;
            Uri[] results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
