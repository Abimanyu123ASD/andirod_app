package cbt.com;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String APP_URL = "https://ct-uat.infinitisoftware.net/";
    private static final int REQUEST_STORAGE_PERMISSION = 123;
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask permission on older Android
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            }
        }

        webView = findViewById(R.id.activity_main_webview);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        // Attach interface to receive blob data
        webView.addJavascriptInterface(new BlobDownloaderInterface(), "AndroidBlob");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectFileSaverOverride();
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl(APP_URL);
    }

    private void injectFileSaverOverride() {
        String jsOverride =
                "if (typeof FileSaver !== 'undefined' && FileSaver.saveAs) {" +
                        "FileSaver._originalSaveAs = FileSaver.saveAs;" +
                        "FileSaver.saveAs = function(blob, name) {" +
                        "  var reader = new FileReader();" +
                        "  reader.onloadend = function() {" +
                        "    var base64data = reader.result.split(',')[1];" +
                        "    AndroidBlob.saveBlobFile(base64data, name);" +
                        "  };" +
                        "  reader.readAsDataURL(blob);" +
                        "};" +
                        "}";
        webView.evaluateJavascript(jsOverride, null);
    }

    private class BlobDownloaderInterface {
        @JavascriptInterface
        public void saveBlobFile(String base64Data, String fileName) {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Saving: " + fileName, Toast.LENGTH_SHORT).show();
            });

            try {
                byte[] fileBytes = Base64.decode(base64Data, Base64.DEFAULT);
                saveToDownloads(fileBytes, fileName);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Saved: " + fileName, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    private void saveToDownloads(byte[] data, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, getMimeTypeFromFileName(fileName));
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri item = resolver.insert(collection, values);

            if (item != null) {
                try (OutputStream out = resolver.openOutputStream(item)) {
                    out.write(data);
                }
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(item, values, null, null);
            } else {
                throw new Exception("Failed to save file");
            }
        } else {
            File path = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            if (!path.exists()) path.mkdirs();
            File file = new File(path, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
        }
    }

    private String getMimeTypeFromFileName(String fileName) {
        String lowered = fileName.toLowerCase();
        if (lowered.endsWith(".pdf")) return "application/pdf";
        if (lowered.endsWith(".csv")) return "text/csv";
        if (lowered.endsWith(".xls") || lowered.endsWith(".xlsx"))
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return "application/octet-stream";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission denied. Downloads may fail!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
