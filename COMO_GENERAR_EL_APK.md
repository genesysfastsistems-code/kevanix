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
3. Subí **todo el contenido de la carpeta `android/`** a ese repositorio.
   - Podés arrastrar los archivos desde la web de GitHub ("Add file" → "Upload files").
4. Andá a la pestaña **Actions** del repositorio. Vas a ver el flujo
   **"Compilar APK KEVANIX"** ejecutándose solo (o tocá "Run workflow").
5. Cuando termine (unos 3–5 min), entrá a la ejecución y descargá el APK desde
   la sección **Artifacts** → `KEVANIX-APK-debug`.
6. Pasá ese `.apk` a tu celular e instalalo (ver "Instalar el APK" más abajo).

> El archivo `.github/workflows/build-apk.yml` ya está incluido y hace todo esto automáticamente.

---

## ✅ Opción B — Con Android Studio (la más simple en tu PC)

1. Descargá e instalá **Android Studio** (gratis): https://developer.android.com/studio
   - Android Studio descarga solo el SDK que falta.
2. Abrí Android Studio → **Open** → elegí la carpeta `android/`.
3. Esperá a que sincronice (Gradle Sync). La primera vez tarda unos minutos.
4. Menú **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Cuando termine, aparece un aviso "locate" → ahí está tu `app-debug.apk`.
   - Ruta: `app/build/outputs/apk/debug/app-debug.apk`

---

## ✅ Opción C — Por línea de comandos

Requisitos: **JDK 17+** y el **Android SDK** instalado, con la variable
`ANDROID_HOME` (o `ANDROID_SDK_ROOT`) apuntando a él.

```bash
cd android
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
| 👑 Dueño / Super usuario | `dueno` | `1234` |
| 🏫 Administración | `admin` | `1234` |
| 👩‍🏫 Docente | `docente` | `1234` |
| 👨‍👩‍👧 Familia | `familia` | `1234` |
| 🎒 Alumno | `alumno` | `1234` |

---

## ⚙️ Personalizar

- **Nombre de la app:** `app/src/main/res/values/strings.xml`
- **Colores / ícono:** `res/values/colors.xml` y `res/drawable/ic_launcher_foreground.xml`
- **Contenido de la app:** `app/src/main/assets/www/index.html`
  (es la app completa en un solo archivo; podés editar textos, precios, módulos, etc.)
- **ID de la app (para Play Store):** `applicationId` en `app/build.gradle`

---

## ⚠️ Importante sobre los datos

Esta versión guarda todo **en el propio teléfono** (almacenamiento local del WebView).
Es perfecta para demostración, uso de un solo dispositivo o piloto.
Para que varios usuarios compartan la misma información en tiempo real
(por ejemplo, que la familia vea las notas que cargó el docente desde otro celular),
hace falta agregar un **servidor / base de datos en la nube**. Ese es el siguiente
paso natural si el proyecto avanza — avisame y lo encaramos.
