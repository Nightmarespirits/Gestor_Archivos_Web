<template>
  <v-footer app class="pa-3 text-center d-flex flex-column modern-footer" style="font-size: 0.9em;">
    <span>
       {{ new Date().getFullYear() }} IESTPFFAA &copy; Made by
      <a
        href="https://github.com/NightmareSpirits"
        target="_blank"
        rel="noopener"
        class="footer-link"
      >
        ns
      </a>
    </span>
  </v-footer>
</template>

<script setup>
// Sin lógica adicional por ahora
</script>

<style scoped>
.footer-link {
  text-decoration: none;
  font-weight: bold;
  transition: color 0.2s;
}
.footer-link:hover {
  color: #0770a0;
  text-decoration: underline;
}
</style>