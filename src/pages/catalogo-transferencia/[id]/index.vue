<template>
<v-container fluid>
    <v-card>
    <v-card-title class="d-flex justify-space-between">
        <span>Detalle de Catálogo de Transferencia</span>
        <div>
        <v-btn
            color="warning"
            prepend-icon="mdi-pencil"
            :to="{ name: 'catalogo-transferencia-edit', params: { id } }"
            class="mr-2"
            v-if="puedeManejarCatalogo"
        >
            Editar
        </v-btn>
        
        <v-btn
            color="primary"
            prepend-icon="mdi-arrow-left"
            :to="{ name: 'catalogo-transferencia-list' }"
        >
            Volver
        </v-btn>
        </div>
    </v-card-title>
    
    <v-card-text>
        <v-alert
        v-if="error"
        type="error"
        title="Error"
        :text="error"
        class="mb-4"
        ></v-alert>
        
        <!--<v-skeleton-loader
        v-if="loading && !catalogo"
        type="card-heading, list-item-three-line, list-item-three-line, list-item-three-line, list-item-three-line"
        ></v-skeleton-loader>
        -->
        <template v-else-if="catalogo">
        <!-- Encabezado principal -->
        <div class="d-flex justify-space-between align-center mb-4">
            <h2 class="text-h5">{{ catalogo.codigoReferencia || 'Sin código' }}</h2>
            <v-chip
            :color="getSoporteColor(catalogo.soporte)"
            text-color="white"
            >
            {{ catalogo.soporte || 'No especificado' }}
            </v-chip>
        </div>
        
        <v-divider class="mb-4"></v-divider>
        
        <!-- Información Principal -->
        <v-row>
            <v-col cols="12" md="6">
            <v-list>
                <v-list-subheader>Información de la Entidad</v-list-subheader>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-domain"></v-icon>
                </template>
                <v-list-item-title>Nombre Entidad</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.nombreEntidad || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-office-building"></v-icon>
                </template>
                <v-list-item-title>Unidad de Organización</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.unidadOrganizacion || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-folder"></v-icon>
                </template>
                <v-list-item-title>Sección</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.seccion || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-layers"></v-icon>
                </template>
                <v-list-item-title>Nivel de Descripción</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.nivelDescripcion || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-file-document-multiple"></v-icon>
                </template>
                <v-list-item-title>Serie Documental</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.serieDocumental || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
            </v-list>
            </v-col>
            
            <v-col cols="12" md="6">
            <v-list>
                <v-list-subheader>Información Técnica</v-list-subheader>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-barcode"></v-icon>
                </template>
                <v-list-item-title>Código de Referencia</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.codigoReferencia || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-file"></v-icon>
                </template>
                <v-list-item-title>Soporte</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.soporte || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-ruler"></v-icon>
                </template>
                <v-list-item-title>Volumen (metros lineales)</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.volumenMetrosLineales || '0' }} m</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-calendar-clock"></v-icon>
                </template>
                <v-list-item-title>Fecha Creación</v-list-item-title>
                <v-list-item-subtitle>{{ formatDate(catalogo.createdAt) }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item v-if="catalogo.updatedAt">
                <template v-slot:prepend>
                    <v-icon icon="mdi-calendar-edit"></v-icon>
                </template>
                <v-list-item-title>Última Actualización</v-list-item-title>
                <v-list-item-subtitle>{{ formatDate(catalogo.updatedAt) }}</v-list-item-subtitle>
                </v-list-item>
            </v-list>
            </v-col>
        </v-row>
        
        <!-- Información de Responsables -->
        <v-divider class="my-4"></v-divider>
        
        <v-row>
            <v-col cols="12" md="6">
            <v-list>
                <v-list-subheader>Responsables</v-list-subheader>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-account-tie"></v-icon>
                </template>
                <v-list-item-title>Responsable de Sección</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.responsableSeccion || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-account-edit"></v-icon>
                </template>
                <v-list-item-title>Elaborado por</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.inventarioElaboradoPor || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-account-check"></v-icon>
                </template>
                <v-list-item-title>Visto Bueno</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.vistoBuenoResponsable || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
            </v-list>
            </v-col>
            
            <v-col cols="12" md="6">
            <v-list>
                <v-list-subheader>Información Adicional</v-list-subheader>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-file-send"></v-icon>
                </template>
                <v-list-item-title>Número/Año Remisión</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.numeroAnioRemision || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
                
                <v-list-item>
                <template v-slot:prepend>
                    <v-icon icon="mdi-map-marker-radius"></v-icon>
                </template>
                <v-list-item-title>Lugar y Fecha de Elaboración</v-list-item-title>
                <v-list-item-subtitle>{{ catalogo.lugarFechaElaboracion || 'No especificado' }}</v-list-item-subtitle>
                </v-list-item>
            </v-list>
            </v-col>
        </v-row>
        
        <!-- Observaciones -->
        <v-row v-if="catalogo.observaciones">
            <v-col cols="12">
            <v-card variant="outlined" color="background">
                <v-card-title class="text-subtitle-1">
                <v-icon start icon="mdi-information-outline"></v-icon>
                Observaciones
                </v-card-title>
                <v-card-text class="text-body-1">
                {{ catalogo.observaciones }}
                </v-card-text>
            </v-card>
            </v-col>
        </v-row>
        
        <!-- Sección de Detalles del Catálogo -->
        <v-divider class="my-6"></v-divider>
        
        <div class="d-flex align-center mb-4">
            <h3 class="text-h5">Detalles del Catálogo</h3>
            <v-spacer></v-spacer>
            <v-chip
            :color="catalogo.detalles?.length > 0 ? 'success' : 'warning'"
            text-color="white"
            >
            {{ catalogo.detalles?.length || 0 }} ítems
            </v-chip>
        </div>
        
        <!-- Tabla de Detalles -->
        <v-card variant="outlined" class="mb-4">
            <v-card-text>
            <v-data-table
                v-if="catalogo.detalles && catalogo.detalles.length > 0"
                :headers="detalleHeaders"
                :items="catalogo.detalles"
                :items-per-page="10" 
                item-value="id"
                class="elevation-0"
            >
                <template v-slot:item.numeroItem="{ item }">
                <v-chip size="small" color="primary" variant="outlined">
                    {{ item.numeroItem }}
                </v-chip>
                </template>
                
                <template v-slot:item.fechaUnidadDocumental="{ item }">
                {{ item.fechaUnidadDocumental ? formatDate(item.fechaUnidadDocumental) : '-' }}
                </template>
                
                <template v-slot:item.alcanceContenido="{ item }">
                <div class="text-truncate" style="max-width: 200px;" :title="item.alcanceContenido">
                    {{ item.alcanceContenido || '-' }}
                </div>
                </template>
                
                <template v-slot:item.cantidadFolios="{ item }">
                <v-chip size="small" color="info" variant="outlined">
                    {{ item.cantidadFolios || 0 }} folios
                </v-chip>
                </template>
                
                <template v-slot:item.actions="{ item }">
                <v-btn
                    icon="mdi-eye"
                    size="small"
                    variant="text"
                    color="primary"
                    @click="verDetalle(item)"
                    :title="`Ver detalle del ítem ${item.numeroItem}`"
                ></v-btn>
                </template>
            </v-data-table>
            
            <v-alert
                v-else
                type="info"
                text="No hay detalles registrados en este catálogo de transferencia."
                variant="tonal"
                class="mt-2"
            ></v-alert>
            </v-card-text>
        </v-card>
        
        <!-- Estadísticas rápidas -->
        <v-row class="mb-4">
            <v-col cols="12" md="3">
            <v-card color="primary" variant="tonal">
                <v-card-text class="text-center">
                <div class="text-h4">{{ catalogo.detalles?.length || 0 }}</div>
                <div class="text-subtitle-1">Total Ítems</div>
                </v-card-text>
            </v-card>
            </v-col>
            
            <v-col cols="12" md="3">
            <v-card color="success" variant="tonal">
                <v-card-text class="text-center">
                <div class="text-h4">{{ totalFolios }}</div>
                <div class="text-subtitle-1">Total Folios</div>
                </v-card-text>
            </v-card>
            </v-col>
            
            <v-col cols="12" md="3">
            <v-card color="info" variant="tonal">
                <v-card-text class="text-center">
                <div class="text-h4">{{ totalCajas }}</div>
                <div class="text-subtitle-1">Total Cajas</div>
                </v-card-text>
            </v-card>
            </v-col>
            
            <v-col cols="12" md="3">
            <v-card color="warning" variant="tonal">
                <v-card-text class="text-center">
                <div class="text-h4">{{ catalogo.volumenMetrosLineales || 0 }}m</div>
                <div class="text-subtitle-1">Metros Lineales</div>
                </v-card-text>
            </v-card>
            </v-col>
        </v-row>
        
        <!-- Botones de acción -->
        <div class="d-flex flex-wrap gap-3">
            <v-btn
            color="primary"
            prepend-icon="mdi-printer"
            @click="descargarCatalogo('pdf')"
            >
            Imprimir PDF
            </v-btn>
            
            <v-btn
            color="success"
            prepend-icon="mdi-microsoft-excel"
            @click="descargarCatalogo('excel')"
            >
            Descargar Excel
            </v-btn>
            
            <v-btn
            v-if="puedeManejarCatalogo"
            color="error"
            prepend-icon="mdi-delete"
            @click="eliminarCatalogo"
            >
            Eliminar
            </v-btn>
        </div>
        </template>
    </v-card-text>
    </v-card>
</v-container>

<!-- Modal para Ver Detalle de Ítem -->
<v-dialog v-model="modalDetalleItem" max-width="900px" scrollable>
    <v-card>
    <v-card-title class="d-flex align-center">
        <v-icon start icon="mdi-file-document-outline"></v-icon>
        Detalle del Ítem {{ detalleSeleccionado?.numeroItem }}
    </v-card-title>
    
    <v-card-text>
        <v-row v-if="detalleSeleccionado">
        <v-col cols="12" md="6">
            <v-list>
            <v-list-subheader>Información Básica</v-list-subheader>
            
            <v-list-item>
                <template v-slot:prepend>
                <v-icon icon="mdi-numeric"></v-icon>
                </template>
                <v-list-item-title>Número de Item</v-list-item-title>
                <v-list-item-subtitle>{{ detalleSeleccionado.numeroItem }}</v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item>
                <template v-slot:prepend>
                <v-icon icon="mdi-package-variant"></v-icon>
                </template>
                <v-list-item-title>Número de Caja</v-list-item-title>
                <v-list-item-subtitle>{{ detalleSeleccionado.numeroCaja || 'No especificado' }}</v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item>
                <template v-slot:prepend>
                <v-icon icon="mdi-book"></v-icon>
                </template>
                <v-list-item-title>Número de Tomo/Paquete</v-list-item-title>
                <v-list-item-subtitle>{{ detalleSeleccionado.numeroTomoPaquete || 'No especificado' }}</v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item>
                <template v-slot:prepend>
                <v-icon icon="mdi-file-document"></v-icon>
                </template>
                <v-list-item-title>Número de Unidad Documental</v-list-item-title>
                <v-list-item-subtitle>{{ detalleSeleccionado.numeroUnidadDocumental || 'No especificado' }}</v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item>
                <template v-slot:prepend>
                <v-icon icon="mdi-calendar"></v-icon>
                </template>
                <v-list-item-title>Fecha de Unidad Documental</v-list-item-title>
                <v-list-item-subtitle>{{ detalleSeleccionado.fechaUnidadDocumental ? formatDate(detalleSeleccionado.fechaUnidadDocumental) : 'No especificado' }}</v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item>
                <template v-slot:prepend>
                <v-icon icon="mdi-file-multiple"></v-icon>
                </template>
                <v-list-item-title>Cantidad de Folios</v-list-item-title>
                <v-list-item-subtitle>{{ detalleSeleccionado.cantidadFolios || 0 }}</v-list-item-subtitle>
            </v-list-item>
            </v-list>
        </v-col>
        
        <v-col cols="12" md="6">
            <v-list>
            <v-list-subheader>Contenido y Observaciones</v-list-subheader>
            
            <v-card variant="outlined" class="mb-3" v-if="detalleSeleccionado.alcanceContenido">
                <v-card-title class="text-subtitle-1">
                <v-icon start icon="mdi-text-box-outline"></v-icon>
                Alcance y Contenido
                </v-card-title>
                <v-card-text class="text-body-2">
                {{ detalleSeleccionado.alcanceContenido }}
                </v-card-text>
            </v-card>
            
            <v-card variant="outlined" class="mb-3" v-if="detalleSeleccionado.informacionAdicional">
                <v-card-title class="text-subtitle-1">
                <v-icon start icon="mdi-information"></v-icon>
                Información Adicional
                </v-card-title>
                <v-card-text class="text-body-2">
                {{ detalleSeleccionado.informacionAdicional }}
                </v-card-text>
            </v-card>
            
            <v-card variant="outlined" v-if="detalleSeleccionado.observaciones">
                <v-card-title class="text-subtitle-1">
                <v-icon start icon="mdi-note-text"></v-icon>
                Observaciones
                </v-card-title>
                <v-card-text class="text-body-2">
                {{ detalleSeleccionado.observaciones }}
                </v-card-text>
            </v-card>
            </v-list>
        </v-col>
        </v-row>
    </v-card-text>
    
    <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
        variant="text"
        @click="modalDetalleItem = false"
        >
        Cerrar
        </v-btn>
    </v-card-actions>
    </v-card>
</v-dialog>
<!-- Snackbar para notificaciones -->
<v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="snackbar.timeout">
    {{ snackbar.text }}
</v-snackbar>

</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCatalogoTransferenciaStore } from '@/store/catalogo-transferencia';  
import { useUserPermissionsStore } from '@/store/userPermissions';
import { useAuthStore } from '@/store/auth';

// Stores
const route = useRoute();
const router = useRouter();
const catalogoTransferenciaStore = useCatalogoTransferenciaStore();
const userPermissionsStore = useUserPermissionsStore();

// Estado reactivo
const id = computed(() => route.params.id);
const loading = computed(() => catalogoTransferenciaStore.isLoading());
const error = ref(null);
const catalogo = computed(() => catalogoTransferenciaStore.getCurrentCatalogoTransferencia());
const modalDetalleItem = ref(false);
const detalleSeleccionado = ref(null);
// Snackbar para notificaciones
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000,
});

// Headers para la tabla de detalles
const detalleHeaders = [
{ title: 'Nº Item', key: 'numeroItem', sortable: true },
{ title: 'Nº Caja', key: 'numeroCaja', sortable: true },
{ title: 'Nº Tomo/Paquete', key: 'numeroTomoPaquete', sortable: true },
{ title: 'Nº Unidad Documental', key: 'numeroUnidadDocumental', sortable: true },
{ title: 'Fecha', key: 'fechaUnidadDocumental', sortable: true },
{ title: 'Contenido', key: 'alcanceContenido', sortable: false },
{ title: 'Folios', key: 'cantidadFolios', sortable: true },
{ title: 'Acciones', key: 'actions', sortable: false, align: 'center' }
];

// Permisos
const puedeManejarCatalogo = computed(() => userPermissionsStore.hasPermission('DOCUMENT_CREATE'));

// Computed para estadísticas
const totalFolios = computed(() => {
if (!catalogo.value?.detalles) return 0;
return catalogo.value.detalles.reduce((total, detalle) => total + (detalle.cantidadFolios || 0), 0);
});

const totalCajas = computed(() => {
if (!catalogo.value?.detalles) return 0;
const cajasUnicas = new Set(
    catalogo.value.detalles
    .map(detalle => detalle.numeroCaja)
    .filter(caja => caja && caja.trim() !== '')
);
return cajasUnicas.size;
});

// Cargar el catálogo al montar el componente
onMounted(async () => {
try {
    const data = await catalogoTransferenciaStore.fetchCatalogoTransferenciaById(id.value);
    console.log("data", data);
    console.log("catalogo.value", catalogoTransferenciaStore.getCurrentCatalogoTransferencia());
    if (!catalogo.value) {
    error.value = 'No se encontró el catálogo de transferencia solicitado';
    }
} catch (err) {
    error.value = `Error al cargar el catálogo: ${err.message}`;
    console.error('Error:', err);
}
});

// Métodos
async function descargarCatalogo(formato) {
try {
    await catalogoTransferenciaStore.exportCatalogoToExcel(id.value);
} catch (err) {
    error.value = `Error al descargar: ${err.message}`;
    console.error('Error:', err);
}
}

async function eliminarCatalogo() {
if (!catalogo.value) return;

if (confirm('¿Está seguro que desea eliminar este catálogo de transferencia?')) {
    try {
    const eliminado = await catalogoTransferenciaStore.deleteCatalogoTransferencia(id.value);
    if (eliminado) {
        showSnackbar(`Documento Eliminado Correctamente:`);
        router.push({ name: 'catalogo-transferencia-list' });
    }
    } catch (err) {
    error.value = `Error al eliminar: ${err.message}`;
    console.error('Error:', err);
    }
}
}

function verDetalle(detalle) {
detalleSeleccionado.value = detalle;
modalDetalleItem.value = true;
}

function formatDate(dateString) {
if (!dateString) return 'N/A';

try {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('es-ES', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
    }).format(date);
} catch (e) {
    return dateString;
}
}

// Método auxiliar para colores de soporte
function getSoporteColor(soporte) {
switch (soporte) {
    case 'PAPEL':
    return 'brown';
    case 'DIGITAL':
    return 'blue';
    case 'AUDIOVISUAL':
    return 'purple';
    case 'MICROFILM':
    return 'grey';
    case 'OTRO':
    return 'orange';
    default:
    return 'grey';
}
}

// Método auxiliar para estado (mantenido del original)
function getEstadoColor(estado) {
switch (estado) {
    case 'BUENO':
    return 'success';
    case 'REGULAR':
    return 'warning';
    case 'MALO':
    return 'error';
    default:
    return 'grey';
}
}
</script>