# KEVANIX — Sistema Educativo

App escolar (web + APK Android) con Firebase. Publicada en **https://educativo.kevanix.com**.

## Novedades v1.1

- **Inscripción online**: link público para las familias (`?inscripcion=<idColegio>`). Admin aprueba (crea el alumno + cuotas) o rechaza y avisa por WhatsApp.
- **Centro de WhatsApp**: envío guiado de cuotas, notas, inasistencias, avisos o mensaje libre, familia por familia, con botón *Siguiente*.
- **Reportes PDF**: multipágina. Cuadro de calificaciones, parciales, planilla de asistencia mensual, faltas, nómina, docentes, deuda, caja por fechas, estado de cuenta y ficha de inscripción.
- **Biblioteca**: material de estudio por materia (archivo hasta 700 KB o enlace Drive/YouTube).
- **Cuotas**: generar por alumno o por grado, anular, año lectivo automático.
- **Correcciones**: número de recibo persistente (la reimpresión conserva el número), adjuntos de tareas en documentos separados (evita el límite de 1 MB), PDF con acentos correctos, eliminar alumnos, buscador de alumnos, teléfonos 09xx convertidos a 5959xx, formularios que no se cierran al tocar afuera.
- **APK**: abre `educativo.kevanix.com`, permite subir fotos/archivos, pantalla sin conexión, abre los PDF/archivos descargados.

## Firebase — regla para la inscripción online

Las familias envían la solicitud **sin usuario**. Agregá esta regla en *Firestore → Reglas* (junto a las que ya tenés):

```
match /kevanix_colegios/{col}/data/{doc} {
  allow create: if doc.matches('insc_.*')
    && request.resource.data.tipoDoc == 'inscripcion'
    && request.resource.data.estado == 'pendiente';
}
```

Alternativa: activar *Authentication → Método de acceso → Anónimo*.
Si la escritura falla, el formulario ofrece enviar la solicitud por WhatsApp a la institución.

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
