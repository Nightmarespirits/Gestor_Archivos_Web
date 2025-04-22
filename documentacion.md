# Documentación API - Búsqueda avanzada de documentos

## Endpoint de búsqueda avanzada

**GET** `/api/documents/search`

### Parámetros disponibles (todos opcionales):
- `title` (string): Busca documentos cuyo título contenga este texto (no sensible a mayúsculas/minúsculas).
- `author` (string): Busca documentos cuyo nombre o apellidos del autor contengan este texto.
- `fromDate` (string, formato `yyyy-MM-dd`): Fecha de inicio (inclusive) para filtrar por fecha de creación.
- `toDate` (string, formato `yyyy-MM-dd`): Fecha de fin (inclusive) para filtrar por fecha de creación.
- `documentTypeId` (long): Filtra por el id del tipo de documento.
- `tags` (lista de string): Filtra documentos que tengan al menos una de las etiquetas indicadas.

### Ejemplo de uso

```http
GET /api/documents/search?title=acta&author=perez&fromDate=2024-01-01&toDate=2024-12-31&documentTypeId=2&tags=importante&tags=oficial
```

### Respuesta
Devuelve una lista de objetos `DocumentDTO` que cumplen con los filtros proporcionados.

### Notas
- Todos los parámetros son opcionales y pueden combinarse.
- El filtro por autor busca tanto en el nombre como en los apellidos del autor.
- Las fechas deben estar en formato `yyyy-MM-dd`.
- El filtro por etiquetas es inclusivo (si el documento tiene al menos una de las etiquetas, será incluido).

---

Si tienes dudas sobre cómo consumir este endpoint desde el frontend, consulta la sección de integración o solicita ejemplos.
