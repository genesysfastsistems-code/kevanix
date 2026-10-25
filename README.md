# KEVANIX — Sistema Educativo

App escolar (web + APK Android) con Firebase. Publicada en **https://educativo.kevanix.com**.

## ⚠️ Pasos al actualizar a v1.2 (en este orden)

1. **Firebase → Firestore → Reglas:** reemplazá todo por el contenido de [`firestore.rules`](firestore.rules) y tocá **Publicar**.
2. **Enseguida, subí los archivos a GitHub** (publica la web y compila el APK). Hacé 1 y 2 juntos, uno detrás del otro.
3. **Entrá una vez como dueño (`kevanix`)**: el sistema migra solo el registro de colegios al formato nuevo.
4. **Cada colegio migra sus datos solo** cuando su administrador entra por primera vez.
5. En el panel del dueño: **eliminá el colegio demo** y **cambiá tu contraseña** (aparece un aviso rojo mientras exista).
6. **Revisá el plan de cada colegio**: ahora los planes Básica y Media limitan alumnos y módulos de verdad. Si querés que un cliente siga con todo, ponelo en Premium/Pro/Máximo.
7. Opcional: en `index.html` completá `KEVANIX_WA` con tu WhatsApp para que los colegios te pidan cambios de plan.

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
