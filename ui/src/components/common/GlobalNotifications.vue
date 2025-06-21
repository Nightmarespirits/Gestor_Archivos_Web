<template>
  <!-- Snackbar para notificaciones -->
  <v-snackbar
    v-model="snackbar.show"
    :color="snackbar.color"
    :timeout="snackbar.timeout"
    :location="'top'"
    class="v-snackbar--global"
  >
    {{ snackbar.text }}
    <template v-slot:actions>
      <v-btn
        color="white"
        variant="text"
        @click="closeSnackbar"
      >
        Cerrar
      </v-btn>
    </template>
  </v-snackbar>

  <!-- Diálogo de confirmación -->
  <v-dialog
    v-model="confirmDialog.show"
    max-width="500px"
    persistent
    @keydown.esc="handleCancel"
  >
    <v-card>
      <v-card-title>
        <span class="text-h5">{{ confirmDialog.title }}</span>
      </v-card-title>
      <v-card-text>
        {{ confirmDialog.message }}
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="grey"
          variant="text"
          @click="handleCancel"
        >
          {{ confirmDialog.cancelText }}
        </v-btn>
        <v-btn
          :color="confirmDialog.confirmColor"
          variant="text"
          @click="handleConfirm"
        >
          {{ confirmDialog.confirmText }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { useNotifications } from '@/composables/useNotifications';

const { snackbar, confirmDialog, closeSnackbar } = useNotifications();

// Manejadores de eventos para el diálogo de confirmación
function handleConfirm() {
  if (typeof confirmDialog.onConfirm === 'function') {
    confirmDialog.onConfirm();
  }
}

function handleCancel() {
  if (typeof confirmDialog.onCancel === 'function') {
    confirmDialog.onCancel();
  }
}
</script>

<style>
.v-snackbar--global {
  z-index: 9999;
}
</style>