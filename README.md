# KEVANIX — Sistema Educativo

App escolar (web + APK Android) con Firebase. Publicada en **https://educativo.kevanix.com**.

## 🚀 Publicación automática

Cada `push` a `main`:
- publica la web en **educativo.kevanix.com** (GitHub Pages),
- compila el APK (*Actions → Artifacts → KEVANIX-APK*),
- publica `firestore.rules` en Firebase (secreto `FIREBASE_SERVICE_ACCOUNT`, rol *Administrador de Firebase*).

Pendientes manuales: eliminar el colegio demo, cambiar la clave del dueño, revisar el plan de cada colegio y completar `KEVANIX_WA` en `index.html`.

## Novedades v1.2

- **Base de datos por registro**: cada colección se reparte en documentos (asistencia por quincena, notas por materia, alumnos por grado…) y cada registro se guarda por separado → sin límite de 1 MB y **dos usuarios ya no se pisan**. Migración automática.
- **Permisos por rol** (`firestore.rules`): familias y alumnos ya no pueden modificar notas ni finanzas; el admin no puede cambiarse el plan ni el vencimiento.
- **Cierre del año lectivo**: promoción por grado (aprobados suben, con aplazos repiten), egresos/bajas, historial académico, archivo de notas, cuotas del año siguiente, Inicial → 1° grado.
- **Hermanos**: una cuenta de familia con varios hijos y selector en la app.
- **Comprobantes de pago**: la familia sube la foto de la transferencia; el admin confirma (cuotas pagadas + caja + recibo + WhatsApp) o rechaza.
- **Asistencia rápida**: "Todos presentes", contador P/A/T y aviso de ausentes por WhatsApp.
- **Excel**: alumnos, calificaciones, asistencia, finanzas y respaldo completo.
- **Notificaciones del celular** (Ajustes → Activar). Con la app cerrada requiere Firebase Cloud Messaging + Cloud Functions (plan Blaze).
- **Recibos con timbrado**: N° de timbrado, establecimiento y punto de expedición (001-001-0000001), C.I./RUC del pagador. *SIFEN (factura electrónica) requiere servidor y certificado digital: no incluido.*
- **Inscripción**: QR, afiche PDF imprimible, cupos por grado, abrir/cerrar inscripción; el formulario muestra el logo del colegio.
- **Panel del dueño → Uso**: alumnos por colegio, último ingreso, colegios inactivos y cerca del límite del plan.
- **Planes reales**: Básica (150 alumnos, sin finanzas), Media (400, con finanzas y WhatsApp), Premium/Pro/Máximo (todo).

## Novedades v1.1

- Inscripción online, Centro de WhatsApp, Reportes PDF multipágina, Biblioteca, generación de cuotas y correcciones varias.

## Cuentas de demostración

| Rol | Usuario | Contraseña |
|-----|---------|-----------|
| 👑 Dueño / Super usuario | `kevanix` | `123456` |
| 🏫 Administración | `admin` | `123456` |
| 👩‍🏫 Docente | `docente` | `123456` |
| 👨‍👩‍👧 Familia | `familia` | `123456` |
| 🎒 Alumno | `alumno` | `123456` |

## Generar el APK

Ver [COMO_GENERAR_EL_APK.md](COMO_GENERAR_EL_APK.md). Con GitHub: cada `push` a `main` compila el APK en *Actions → Artifacts*.
