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
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.app.Activity;
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
    // Carga la versión EN LÍNEA del Sistema Educativo (dominio propio, ver archivo CNAME).
    // Así el APK se actualiza solo cada vez que se publica la web.
    private static final String HOME = "https://educativo.kevanix.com/";
    // Página local que se muestra si no hay internet
    private static final String OFFLINE = "file:///android_asset/www/offline.html";
    private static final int FILE_CHOOSER = 7001;
    private ValueCallback<Uri[]> filePathCallback;

    // Hosts que se abren DENTRO de la app (dominio propio + respaldo al dominio viejo de GitHub)
    private static boolean esAppHost(String host) {
        return host.equals("educativo.kevanix.com")
            || host.equals("www.kevanix.com")
            || host.equals("kevanix.com")
            || host.endsWith(".kevanix.com")
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

        // WebChromeClient con selector de archivos: permite subir fotos, logos,
        // tareas y material de la biblioteca desde el celular (galería, cámara, archivos)
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = cb;
                try {
                    Intent i = params.createIntent();
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    if (params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE)
                        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    String[] acc = params.getAcceptTypes();
                    boolean soloImg = acc != null && acc.length > 0 && acc[0] != null && acc[0].startsWith("image");
                    if (!soloImg) i.setType("*/*");
                    startActivityForResult(Intent.createChooser(i, "Elegir archivo"), FILE_CHOOSER);
                } catch (Exception e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "No se pudo abrir el selector de archivos", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });
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

            // Sin internet: mostrar pantalla local con botón "Reintentar"
            @Override
            public void onReceivedError(WebView v, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) v.loadUrl(OFFLINE);
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

    // Resultado del selector de archivos
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER || filePathCallback == null) return;
        Uri[] res = null;
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int n = data.getClipData().getItemCount();
                res = new Uri[n];
                for (int k = 0; k < n; k++) res[k] = data.getClipData().getItemAt(k).getUri();
            } else if (data.getData() != null) {
                res = new Uri[]{ data.getData() };
            }
        }
        filePathCallback.onReceiveValue(res);
        filePathCallback = null;
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
            saveFile(filename, base64, "application/pdf");
        }

        // Guarda cualquier archivo (PDF, imágenes, Word...) en Descargas y lo abre
        @JavascriptInterface
        public void saveFile(String filename, String base64, String mime) {
            if (mime == null || mime.isEmpty()) mime = "application/octet-stream";
            try {
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues cv = new ContentValues();
                    cv.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                    cv.put(MediaStore.Downloads.MIME_TYPE, mime);
                    cv.put(MediaStore.Downloads.IS_PENDING, 1);
                    Uri col = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    Uri item = ctx.getContentResolver().insert(col, cv);
                    OutputStream os = ctx.getContentResolver().openOutputStream(item);
                    os.write(data); os.close();
                    cv.clear(); cv.put(MediaStore.Downloads.IS_PENDING, 0);
                    ctx.getContentResolver().update(item, cv, null, null);
                    abrir(item, mime);
                } else {
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!dir.exists()) dir.mkdirs();
                    File f = new File(dir, filename);
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(data); fos.close();
                }
                toast("Guardado en Descargas: " + filename);
            } catch (Exception e) {
                toast("Error al guardar el archivo");
            }
        }
        private void abrir(final Uri uri, final String mime) {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    Intent v = new Intent(Intent.ACTION_VIEW);
                    v.setDataAndType(uri, mime);
                    v.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(v);
                } catch (Exception ignored) { /* sin app para abrirlo: queda en Descargas */ }
            });
        }
        private void toast(final String msg) {
            new Handler(Looper.getMainLooper()).post(
                () -> Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show());
        }
    }
}
