<template id="not-found">
  <app-frame>
    <h1>We can't find the page you're looking for<h1>
    <h2><small>(error 404)</small></h2>
  </app-frame>
</template>

<script>
Vue.component("not-found", {
  template: "#not-found"
});
</script>

<style>
small {
  color: #555;
}
</style>
