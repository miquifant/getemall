<template id="app-frame">
  <div class="app-frame">
    <slot></slot>
  </div>
</template>

<script>
Vue.component("app-frame", {
  template: "#app-frame"
});
</script>

<style>
.app-frame {
  max-width: 800px;
  margin: 0 auto;
}
</style>
