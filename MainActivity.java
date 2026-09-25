package com.teachernelly.younglearners;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String ACADEMY_URL = "https://teachernellyacademi.netlify.app/";
    private static final int FILE_CHOOSER_REQUEST = 1001;

    private WebView webView;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> filePathCallback;
    private boolean webMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showNativeHome();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return t;
    }

    private Button actionButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.rgb(91, 24, 214));
        b.setPadding(dp(8), dp(8), dp(8), dp(8));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(56), 1f);
        p.setMargins(dp(5), dp(5), dp(5), dp(5));
        b.setLayoutParams(p);
        return b;
    }

    private void showNativeHome() {
        webMode = false;
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(24));
        root.setBackgroundColor(Color.rgb(248, 246, 252));
        scroll.addView(root);

        TextView title = text("Teacher Nelly’s Young Learners Academy", 24, Color.rgb(55, 18, 110), true);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView slogan = text("Learn • Practise • Play • Grow 🌟", 15, Color.DKGRAY, false);
        slogan.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(0, dp(6), 0, dp(20));
        root.addView(slogan, sp);

        TextView intro = text("Your learning space for lessons, quizzes, registration and premium learning.", 16, Color.DKGRAY, false);
        intro.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.setMargins(0, 0, 0, dp(18));
        root.addView(intro, ip);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        Button learn = actionButton("📚  Learn");
        Button register = actionButton("📝  Register");
        row1.addView(learn); row1.addView(register); root.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        Button quizzes = actionButton("🧠  Quizzes");
        Button premium = actionButton("⭐  Premium");
        row2.addView(quizzes); row2.addView(premium); root.addView(row2);

        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        Button parent = actionButton("👨‍👩‍👧  Parent Area");
        Button downloads = actionButton("📥  Downloads");
        row3.addView(parent); row3.addView(downloads); root.addView(row3);

        LinearLayout row4 = new LinearLayout(this);
        row4.setOrientation(LinearLayout.HORIZONTAL);
        Button share = actionButton("📤  Share Academy");
        Button website = actionButton("🌐  Website");
        row4.addView(share); row4.addView(website); root.addView(row4);

        TextView about = text("About the app\n\nTeacher Nelly’s Young Learners Academy brings lessons and learning activities together with a dedicated Android experience. Internet access is required for online academy content.", 14, Color.DKGRAY, false);
        about.setPadding(dp(8), dp(20), dp(8), dp(8));
        root.addView(about);

        View.OnClickListener openAcademy = v -> openAcademyPage();
        learn.setOnClickListener(openAcademy);
        register.setOnClickListener(openAcademy);
        quizzes.setOnClickListener(openAcademy);
        premium.setOnClickListener(openAcademy);
        parent.setOnClickListener(openAcademy);
        website.setOnClickListener(openAcademy);
        downloads.setOnClickListener(v -> showDownloadsInfo());
        share.setOnClickListener(v -> shareAcademy());

        setContentView(scroll);
    }

    private void openAcademyPage() {
        showWebView();
        if (webView.getUrl() == null) webView.loadUrl(ACADEMY_URL);
        else webView.reload();
    }

    private void shareAcademy() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Teacher Nelly’s Young Learners Academy");
        send.putExtra(Intent.EXTRA_TEXT, "Learn • Practise • Play • Grow 🌟\n" + ACADEMY_URL);
        startActivity(Intent.createChooser(send, "Share Academy"));
    }

    private void showDownloadsInfo() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Academy Downloads")
                .setMessage("Files downloaded from the academy are handled by Android’s Downloads system. If a lesson provides a downloadable file, tap its download link inside the academy.")
                .setPositiveButton("Open Academy", (d, w) -> openAcademyPage())
                .setNegativeButton("Close", null)
                .show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showWebView() {
        webMode = true;
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(4), dp(3), dp(4), dp(3));
        toolbar.setBackgroundColor(Color.rgb(91, 24, 214));

        Button home = new Button(this);
        home.setText("Home");
        home.setAllCaps(false);
        Button refresh = new Button(this);
        refresh.setText("Refresh");
        refresh.setAllCaps(false);
        Button share = new Button(this);
        share.setText("Share");
        share.setAllCaps(false);
        toolbar.addView(home, new LinearLayout.LayoutParams(0, dp(48), 1));
        toolbar.addView(refresh, new LinearLayout.LayoutParams(0, dp(48), 1));
        toolbar.addView(share, new LinearLayout.LayoutParams(0, dp(48), 1));
        container.addView(toolbar);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        container.addView(progressBar, new LinearLayout.LayoutParams(-1, dp(3)));

        webView = new WebView(this);
        container.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(container);

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

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "academy_download_" + System.currentTimeMillis());
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Download could not start", Toast.LENGTH_SHORT).show();
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) { }
                return true;
            }
        });

        home.setOnClickListener(v -> showNativeHome());
        refresh.setOnClickListener(v -> webView.reload());
        share.setOnClickListener(v -> shareAcademy());
        webView.loadUrl(ACADEMY_URL);
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
    public void onBackPressed() {
        if (webMode && webView != null && webView.canGoBack()) {
            webView.goBack();
        } else if (webMode) {
            showNativeHome();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }
}
