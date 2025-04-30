<template>
  <v-card>
    <v-card-title>Ejemplo de uso del Sistema de Permisos</v-card-title>
    <v-card-text>
      <p>Este componente demuestra cómo integrar el sistema de permisos en cualquier parte de tu aplicación.</p>
      
      <div class="d-flex flex-wrap gap-2 mt-4">
        <!-- Botón de Ver - visible para cualquiera con permiso DOCUMENT_READ -->
        <PermissionButton 
          :permissions="['DOCUMENT_READ']"
          color="primary" 
          prepend-icon="mdi-eye"
          @click="onViewClick"
        >
          Ver documento
        </PermissionButton>
        
        <!-- Botón de Descargar - visible para cualquiera con permiso FILE_DOWNLOAD -->
        <PermissionButton 
          :permissions="['FILE_DOWNLOAD']"
          color="info" 
          prepend-icon="mdi-download"
          @click="onDownloadClick"
        >
          Descargar
        </PermissionButton>
        
        <!-- Botón de Editar - visible para cualquiera con permiso DOCUMENT_UPDATE -->
        <PermissionButton 
          :permissions="['DOCUMENT_UPDATE']"
          color="warning" 
          prepend-icon="mdi-pencil"
          @click="onEditClick"
        >
          Editar
        </PermissionButton>
        
        <!-- Botón de Eliminar - visible para cualquiera con permiso DOCUMENT_DELETE -->
        <PermissionButton 
          :permissions="['DOCUMENT_DELETE']"
          color="error" 
          prepend-icon="mdi-delete"
          @click="onDeleteClick"
          variant="outlined"
        >
          Eliminar
        </PermissionButton>
        
        <!-- Botón de Auditoría - visible solo para roles administrativos -->
        <PermissionButton 
          :roles="['ADMIN', 'SUPERADMIN']"
          color="secondary" 
          prepend-icon="mdi-history"
          @click="onAuditClick"
        >
          Ver auditoría
        </PermissionButton>
        
        <!-- Botón de Administración Avanzada - visible solo para SUPERADMIN -->
        <PermissionButton 
          :roles="['SUPERADMIN']"
          color="deep-purple" 
          prepend-icon="mdi-cog"
          @click="onAdvancedSettingsClick"
        >
          Configuración avanzada
        </PermissionButton>
        
        <!-- Ejemplo con múltiples permisos (necesita uno cualquiera de la lista) -->
        <PermissionButton 
          :permissions="['DOCUMENT_CREATE', 'DOCUMENT_UPDATE']"
          color="success" 
          prepend-icon="mdi-plus"
          @click="onCreateOrUpdateClick"
        >
          Crear/Editar
        </PermissionButton>
        
        <!-- Ejemplo con múltiples permisos requeridos (necesita todos) -->
        <PermissionButton 
          :require-all-permissions="['FILE_UPLOAD', 'DOCUMENT_CREATE']"
          color="teal" 
          prepend-icon="mdi-upload"
          @click="onUploadNewDocumentClick"
        >
          Subir nuevo documento
        </PermissionButton>
      </div>
    </v-card-text>
    
    <!-- Componente que usa v-permission directiva -->
    <v-divider></v-divider>
    <v-card-text>
      <h3 class="text-subtitle-1 mb-2">Elementos con Directiva v-permission</h3>
      
      <div class="d-flex flex-wrap gap-2">
        <v-btn color="primary" v-permission="'DOCUMENT_READ'">
          Ver (Directiva)
        </v-btn>
        
        <v-btn color="error" v-permission.role="'SUPERADMIN'">
          Solo Superadmin
        </v-btn>
        
        <v-btn color="warning" v-permission="['DOCUMENT_UPDATE', 'DOCUMENT_DELETE']">
          Editar/Eliminar
        </v-btn>
        
        <v-btn color="success" v-permission.all="['FILE_UPLOAD', 'DOCUMENT_CREATE']">
          Todos los permisos
        </v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup>
import { useUserPermissionsStore } from '@/store/userPermissions';
import PermissionButton from '@/components/common/PermissionButton.vue';

const permissionStore = useUserPermissionsStore();

// Funciones de ejemplo para las acciones
const onViewClick = () => {
  console.log('Ver documento');
};

const onDownloadClick = () => {
  console.log('Descargar documento');
};

const onEditClick = () => {
  console.log('Editar documento');
};

const onDeleteClick = () => {
  console.log('Eliminar documento');
};

const onAuditClick = () => {
  console.log('Ver historial de auditoría');
};

const onAdvancedSettingsClick = () => {
  console.log('Configuración avanzada');
};

const onCreateOrUpdateClick = () => {
  console.log('Crear o actualizar documento');
};

const onUploadNewDocumentClick = () => {
  console.log('Subir nuevo documento');
};
</script>

<style scoped>
.gap-2 {
  gap: 8px;
}
</style>
