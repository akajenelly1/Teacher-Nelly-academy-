package com.teachernelly.younglearners;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    private ValueCallback<Uri[]> filePathCallback;

    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int AUDIO_PERMISSION_REQUEST = 1002;

    private static final String ACADEMY_URL =
            "https://teachernellyacademi.netlify.app/";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        configureWebView();

        webView.loadUrl(ACADEMY_URL);

        setupBackButton();

        requestAudioPermission();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);

        settings.setDomStorageEnabled(true);

        settings.setDatabaseEnabled(true);

        settings.setAllowFileAccess(true);

        settings.setAllowContentAccess(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setSupportMultipleWindows(false);

        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setBuiltInZoomControls(false);

        settings.setDisplayZoomControls(false);

        settings.setLoadWithOverviewMode(false);

        settings.setUseWideViewPort(false);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        CookieManager cookieManager = CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new AcademyWebViewClient());

        webView.setWebChromeClient(new AcademyWebChromeClient());

        webView.setDownloadListener(
                (url, userAgent, contentDisposition, mimeType, contentLength) -> {

                    try {

                        Intent intent =
                                new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                        startActivity(intent);

                    } catch (Exception ignored) {
                    }
                }
        );
    }

    private class AcademyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view,
                WebResourceRequest request
        ) {

            Uri uri = request.getUrl();

            String scheme = uri.getScheme();

            if (scheme == null) {
                return false;
            }

            if (
                    scheme.equals("http") ||
                    scheme.equals("https")
            ) {

                return false;
            }

            openExternalLink(uri);

            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view,
                String url
        ) {

            Uri uri = Uri.parse(url);

            String scheme = uri.getScheme();

            if (scheme == null) {
                return false;
            }

            if (
                    scheme.equals("http") ||
                    scheme.equals("https")
            ) {

                return false;
            }

            openExternalLink(uri);

            return true;
        }
    }

    private class AcademyWebChromeClient extends WebChromeClient {

        @Override
        public void onPermissionRequest(
                final PermissionRequest request
        ) {

            runOnUiThread(() -> {

                if (request.getResources() != null) {

                    request.grant(request.getResources());
                }
            });
        }

        @Override
        public boolean onShowFileChooser(
                WebView webView,
                ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams
        ) {

            if (MainActivity.this.filePathCallback != null) {

                MainActivity.this.filePathCallback.onReceiveValue(null);
            }

            MainActivity.this.filePathCallback = filePathCallback;

            Intent intent;

            try {

                intent = fileChooserParams.createIntent();

                startActivityForResult(
                        intent,
                        FILE_CHOOSER_REQUEST
                );

            } catch (ActivityNotFoundException e) {

                MainActivity.this.filePathCallback = null;

                return false;
            }

            return true;
        }
    }

    private void openExternalLink(Uri uri) {

        try {

            Intent intent =
                    new Intent(Intent.ACTION_VIEW, uri);

            startActivity(intent);

        } catch (Exception ignored) {
        }
    }

    private void requestAudioPermission() {

        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                )
                != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    AUDIO_PERMISSION_REQUEST
            );
        }
    }

    private void setupBackButton() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (webView.canGoBack()) {

                            webView.goBack();

                        } else {

                            finish();
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == FILE_CHOOSER_REQUEST) {

            Uri[] results = null;

            if (
                    resultCode == Activity.RESULT_OK &&
                    data != null
            ) {

                Uri result = data.getData();

                if (result != null) {

                    results = new Uri[]{result};
                }
            }

            if (filePathCallback != null) {

                filePathCallback.onReceiveValue(results);

                filePathCallback = null;
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {

            webView.stopLoading();

            webView.setWebChromeClient(null);

            webView.setWebViewClient(null);

            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }
      }
