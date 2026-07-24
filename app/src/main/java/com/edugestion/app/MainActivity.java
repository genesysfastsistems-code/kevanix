package com.edugestion.app;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView web;
    // Carga la versión EN LÍNEA (dominio propio). Así el APK se actualiza solo con la web.
    private static final String HOME = "https://www.kevanix.com/";

    // Hosts que se abren DENTRO de la app (dominio propio + respaldo al dominio viejo de GitHub)
    private static boolean esAppHost(String host) {
        return host.equals("www.kevanix.com")
            || host.equals("kevanix.com")
            || host.equals("genesysfastsistems-code.github.io");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Permiso de almacenamiento (para guardar PDFs en dispositivos viejos)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);           // habilita localStorage (guarda los datos)
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new PdfBridge(this), "AndroidPDF");

        // Abre WhatsApp, teléfono, mail y enlaces externos fuera del WebView
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                Uri u = req.getUrl();
                String url = u.toString();
                String scheme = u.getScheme() == null ? "" : u.getScheme();
                String host = u.getHost() == null ? "" : u.getHost();
                // Esquemas externos (WhatsApp, teléfono, mail)
                if (scheme.equals("whatsapp") || scheme.equals("tel") || scheme.equals("mailto")
                    || scheme.equals("sms") || scheme.equals("intent")) { openExternal(url); return true; }
                if (host.contains("wa.me")) { openExternal(url); return true; }
                // La propia app y la infraestructura de Firebase/Google: cargar dentro del WebView
                if (esAppHost(host) || host.isEmpty()
                    || host.endsWith("gstatic.com") || host.endsWith("googleapis.com")
                    || host.endsWith("google.com") || host.endsWith("firebaseio.com")
                    || host.endsWith("firebaseapp.com") || host.endsWith("cloudfunctions.net")) {
                    return false;
                }
                // Cualquier otro enlace externo (ej: PDFs de la biblioteca en Drive) -> abrir afuera
                if (scheme.startsWith("http")) { openExternal(url); return true; }
                return false;
            }
        });

        // Descarga de PDFs generados por la app (data: y blob:)
        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                try {
                    if (url.startsWith("blob:") || url.startsWith("data:")) {
                        // Los blobs se descargan vía JS (ver inyección abajo)
                        Toast.makeText(MainActivity.this, "Generando PDF...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
                    String name = URLUtil.guessFileName(url, contentDisposition, mimetype);
                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                    ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
                    Toast.makeText(MainActivity.this, "Descargando " + name, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "No se pudo descargar", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Botón atrás: navega dentro del WebView
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (web.canGoBack()) web.goBack();
                else { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); }
            }
        });

        web.loadUrl(HOME);
    }

    private void openExternal(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "No hay app para abrir este enlace", Toast.LENGTH_SHORT).show();
        }
    }

    // Puente para guardar PDFs generados en JS (base64) en la carpeta Descargas
    public static class PdfBridge {
        private final Context ctx;
        PdfBridge(Context c) { this.ctx = c; }

        @JavascriptInterface
        public void save(String filename, String base64) {
            try {
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues cv = new ContentValues();
                    cv.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                    cv.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                    cv.put(MediaStore.Downloads.IS_PENDING, 1);
                    Uri col = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    Uri item = ctx.getContentResolver().insert(col, cv);
                    OutputStream os = ctx.getContentResolver().openOutputStream(item);
                    os.write(data); os.close();
                    cv.clear(); cv.put(MediaStore.Downloads.IS_PENDING, 0);
                    ctx.getContentResolver().update(item, cv, null, null);
                } else {
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!dir.exists()) dir.mkdirs();
                    File f = new File(dir, filename);
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(data); fos.close();
                }
                toast("PDF guardado en Descargas: " + filename);
            } catch (Exception e) {
                toast("Error al guardar PDF");
            }
        }
        private void toast(final String msg) {
            new Handler(Looper.getMainLooper()).post(
                () -> Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show());
        }
    }
}
