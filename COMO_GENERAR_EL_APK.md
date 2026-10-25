# 📱 KEVANIX — Cómo generar el APK

Este proyecto es la app escolar **KEVANIX** empaquetada como aplicación Android
(un WebView que carga la app, que ya viene incluida dentro en `app/src/main/assets/www/`).

No pude compilar el `.apk` en el entorno donde se creó (sin acceso al Android SDK),
así que acá tenés **tres formas** de generarlo. Elegí la que te resulte más cómoda.

---

## ✅ Opción A — En la nube, SIN instalar nada (recomendada)

Ideal si no querés instalar programas. Usa GitHub Actions (gratis).

1. Creá una cuenta en https://github.com (si no tenés).
2. Creá un repositorio nuevo (por ejemplo `edugestion`).
3. Subí **todo el contenido de este proyecto** a ese repositorio.
   - Podés arrastrar los archivos desde la web de GitHub ("Add file" → "Upload files").
4. Andá a la pestaña **Actions** del repositorio. Vas a ver el flujo
   **"Compilar APK KEVANIX"** ejecutándose solo (o tocá "Run workflow").
5. Cuando termine (unos 3–5 min), entrá a la ejecución y descargá el APK desde
   la sección **Artifacts** → `KEVANIX-APK-debug`.
6. Pasá ese `.apk` a tu celular e instalalo (ver "Instalar el APK" más abajo).

> El archivo `.github/workflows/main.yml` ya está incluido y hace todo esto automáticamente.

---

## ✅ Opción B — Con Android Studio (la más simple en tu PC)

1. Descargá e instalá **Android Studio** (gratis): https://developer.android.com/studio
   - Android Studio descarga solo el SDK que falta.
2. Abrí Android Studio → **Open** → elegí la carpeta del proyecto.
3. Esperá a que sincronice (Gradle Sync). La primera vez tarda unos minutos.
4. Menú **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Cuando termine, aparece un aviso "locate" → ahí está tu `app-debug.apk`.
   - Ruta: `app/build/outputs/apk/debug/app-debug.apk`

---

## ✅ Opción C — Por línea de comandos

Requisitos: **JDK 17+** y el **Android SDK** instalado, con la variable
`ANDROID_HOME` (o `ANDROID_SDK_ROOT`) apuntando a él.

```bash
./build-apk.sh          # o: ./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📥 Instalar el APK en el celular

1. Pasá el archivo `.apk` al teléfono (por cable, WhatsApp, Drive, etc.).
2. Abrilo desde el celular. Android va a pedir permitir
   **"Instalar apps de orígenes desconocidos"** → aceptá.
3. Instalá y abrí **KEVANIX**.

> Los APK "debug" y "release sin firmar" sirven para probar e instalar manualmente.
> Para publicar en **Google Play** necesitás firmar el APK/AAB con tu clave
> (Android Studio: *Build → Generate Signed Bundle / APK*).

---

## 🔑 Cuentas de demostración

| Rol | Usuario | Contraseña |
|-----|---------|-----------|
| 👑 Dueño / Super usuario | `kevanix` | `123456` |
| 🏫 Administración | `admin` | `123456` |
| 👩‍🏫 Docente | `docente` | `123456` |
| 👨‍👩‍👧 Familia | `familia` | `123456` |
| 🎒 Alumno | `alumno` | `123456` |

---

## ⚙️ Personalizar

- **Nombre de la app:** `app/src/main/res/values/strings.xml`
- **Colores / ícono:** `res/values/colors.xml` y `res/drawable/ic_launcher_foreground.xml`
- **Contenido de la app:** `app/src/main/assets/www/index.html`
  (es la app completa en un solo archivo; podés editar textos, precios, módulos, etc.)
- **ID de la app (para Play Store):** `applicationId` en `app/build.gradle`

---

## 🌐 Cómo funciona el APK

- El APK abre la versión **en línea**: `https://educativo.kevanix.com/` (definido en `MainActivity.java`).
  Cada vez que publicás la web, el APK se actualiza solo (no hace falta reinstalar).
- Solo hace falta generar un APK nuevo si cambia `MainActivity.java` o el ícono/nombre.
  **v1.1 (versionCode 2) requiere reinstalar** para habilitar: subir fotos/archivos,
  pantalla sin conexión y abrir automáticamente los PDF descargados.
- Los datos están en la nube (Firebase): todos los celulares ven la misma información en tiempo real.
- `app/src/main/assets/www/index.html` es una copia de respaldo de la app; `offline.html` es la pantalla sin internet.
