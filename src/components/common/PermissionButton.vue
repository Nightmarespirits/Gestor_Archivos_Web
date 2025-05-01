<template>
  <PermissionWrapper :permissions="permissions" :roles="roles" :requireAllPermissions="requireAllPermissions">
    <v-btn
      :color="color"
      :variant="variant"
      :size="size"
      :disabled="disabled"
      :class="buttonClass"
      :icon="iconButton"
      :type="type"
      @click="handleClick"
    >
      <v-icon v-if="prependIcon" :icon="prependIcon" class="mr-1"></v-icon>
      <slot v-if="!iconButton">{{ text }}</slot>
    </v-btn>
  </PermissionWrapper>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import PermissionWrapper from './PermissionWrapper.vue';

const props = defineProps({
  // Permisos y roles (heredados de PermissionWrapper)
  permissions: {
    type: [String, Array],
    default: () => []
  },
  requireAllPermissions: {
    type: [String, Array],
    default: () => []
  },
  roles: {
    type: [String, Array],
    default: () => []
  },
  // Propiedades del botón
  text: {
    type: String,
    default: ''
  },
  color: {
    type: String,
    default: 'primary'
  },
  variant: {
    type: String,
    default: 'elevated'
  },
  size: {
    type: String,
    default: 'default'
  },
  prependIcon: {
    type: String,
    default: null
  },
  iconButton: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  buttonClass: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'button'
  }
});

const emit = defineEmits(['click']);

const handleClick = (event) => {
  emit('click', event);
};
</script>
