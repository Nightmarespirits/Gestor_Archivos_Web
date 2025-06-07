#!/bin/bash

# Script para probar los endpoints del controlador de inventarios
# Creado para el sistema ARCHIVO_IESTPFFAA

# Configuración
SERVER_URL="http://localhost:8080"
AUTH_ENDPOINT="/api/auth/login"
USERNAME="superadmin"  # Cambia esto a un usuario válido
PASSWORD="superadmin"  # Cambia esto a una contraseña válida
OUTPUT_DIR="./output"  # Directorio para guardar respuestas y archivos

# Crear directorio de salida si no existe
mkdir -p $OUTPUT_DIR

# Colores para mejor legibilidad
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar mensajes de información
info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Función para mostrar mensajes de éxito
success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Función para mostrar mensajes de error
error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Paso 1: Autenticación para obtener token
info "Iniciando proceso de autenticación..."
AUTH_RESPONSE=$(curl -s -X POST "${SERVER_URL}${AUTH_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")

# Verificar si la autenticación fue exitosa
if [[ $AUTH_RESPONSE == *"token"* ]]; then
    # Extraer el token de la respuesta (esto depende del formato exacto de respuesta)
    TOKEN=$(echo $AUTH_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    success "Autenticación exitosa. Token obtenido."
    echo $AUTH_RESPONSE > $OUTPUT_DIR/auth_response.json
else
    error "Fallo en la autenticación. Respuesta:"
    echo $AUTH_RESPONSE
    exit 1
fi

# Función para realizar solicitudes con autenticación
auth_request() {
    METHOD=$1
    ENDPOINT=$2
    DATA=$3
    OUTPUT_FILE=$4
    
    CURL_CMD="curl -s -X $METHOD \"${SERVER_URL}${ENDPOINT}\" -H \"Authorization: Bearer $TOKEN\" -H \"Content-Type: application/json\""
    
    if [ ! -z "$DATA" ]; then
        CURL_CMD="$CURL_CMD -d '$DATA'"
    fi
    
    info "Realizando solicitud $METHOD a $ENDPOINT"
    RESPONSE=$(eval $CURL_CMD)
    
    # Guardar respuesta en archivo si se especificó
    if [ ! -z "$OUTPUT_FILE" ]; then
        echo $RESPONSE > "$OUTPUT_DIR/$OUTPUT_FILE"
        success "Respuesta guardada en $OUTPUT_DIR/$OUTPUT_FILE"
    fi
    
    echo $RESPONSE
}

# Función para descargar archivos
download_file() {
    ENDPOINT=$1
    OUTPUT_FILE=$2
    
    info "Descargando archivo desde $ENDPOINT"
    curl -s -X GET "${SERVER_URL}${ENDPOINT}" \
        -H "Authorization: Bearer $TOKEN" \
        --output "$OUTPUT_DIR/$OUTPUT_FILE"
    
    if [ $? -eq 0 ]; then
        success "Archivo guardado en $OUTPUT_DIR/$OUTPUT_FILE"
    else
        error "Error al descargar el archivo"
    fi
}

# Paso 2: Obtener todos los inventarios generales
info "Obteniendo listado de inventarios generales..."
INVENTARIOS=$(auth_request "GET" "/api/inventarios/general" "" "inventarios_generales.json")

# Verificar si la respuesta contiene datos
if [[ $INVENTARIOS == "[]" ]]; then
    info "No hay inventarios generales registrados."
else
    success "Listado de inventarios generales obtenido."
    # Extraer el primer ID (si existe) para usarlo en las siguientes solicitudes
    FIRST_ID=$(echo $INVENTARIOS | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    if [ ! -z "$FIRST_ID" ]; then
        info "ID del primer inventario: $FIRST_ID"
    fi
fi

# Paso 3: Crear un nuevo inventario general
info "Creando un nuevo inventario general..."
CURRENT_DATE=$(date +%Y-%m-%d)
CREATE_DATA='{
    "titulo": "Inventario de prueba - '"$CURRENT_DATE"'",
    "unidadAdministrativa": "Unidad de Prueba",
    "numeroAnioRemision": "2025-001",
    "seccion": "Sección de Prueba",
    "fechaTransferencia": "'"$CURRENT_DATE"'",
    "totalVolumen": 1.5,
    "lugarFechaEntrega": "Lima, '"$CURRENT_DATE"'",
    "lugarFechaRecepcion": "Lima, '"$CURRENT_DATE"'",
    "firmaSelloAutoridadEntrega": "Juan Pérez",
    "firmaSelloAutoridadRecibe": "María López",
    "estadoConservacion": "Bueno",
    "observaciones": "Inventario creado por script de prueba",
    "documentoIds": []
}'

CREATED_INVENTARIO=$(auth_request "POST" "/api/inventarios/general" "$CREATE_DATA" "inventario_creado.json")

# Verificar si la creación fue exitosa
if [[ $CREATED_INVENTARIO == *"id"* ]]; then
    CREATED_ID=$(echo $CREATED_INVENTARIO | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    success "Inventario general creado con ID: $CREATED_ID"
else
    error "Error al crear inventario general. Respuesta:"
    echo $CREATED_INVENTARIO
fi

# Paso 4: Obtener el inventario creado por ID (si existe)
if [ ! -z "$CREATED_ID" ]; then
    info "Obteniendo detalle del inventario creado (ID: $CREATED_ID)..."
    auth_request "GET" "/api/inventarios/general/$CREATED_ID" "" "inventario_detalle.json"
    
    # Paso 5: Descargar el archivo Excel del inventario (si existe)
    info "Descargando archivo Excel del inventario..."
    download_file "/api/inventarios/general/$CREATED_ID/download" "inventario_$CREATED_ID.xlsx"
    
    # Paso 6: Descargar el archivo PDF del inventario (si está implementado)
    info "Descargando archivo PDF del inventario..."
    download_file "/api/inventarios/general/$CREATED_ID/pdf" "inventario_$CREATED_ID.pdf"
fi

# Resumen final
echo ""
echo "============================================"
echo "         RESUMEN DE OPERACIONES            "
echo "============================================"
echo "Autenticación: Completada"
echo "Listar inventarios: Completado"
echo "Crear inventario: Completado"
if [ ! -z "$CREATED_ID" ]; then
    echo "ID del inventario creado: $CREATED_ID"
    echo "Detalles del inventario: Guardado en $OUTPUT_DIR/inventario_detalle.json"
    echo "Archivo Excel: Guardado en $OUTPUT_DIR/inventario_$CREATED_ID.xlsx"
    echo "Archivo PDF: Guardado en $OUTPUT_DIR/inventario_$CREATED_ID.pdf"
fi
echo "============================================"
echo "El script se ejecutó correctamente"
echo "Los resultados se han guardado en el directorio: $OUTPUT_DIR"
