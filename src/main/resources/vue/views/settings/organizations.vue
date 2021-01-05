<template id="settings-organizations">
  <settings>
    <h2 class="settings-title">Organizations</h2>
    <pre>TODO</pre>
  </settings>
</template>

<script>
Vue.component("settings-organizations", {
  template: "#settings-organizations",
  data: () => ({
    profile: null
  }),
  mounted: function() {
    $("#menu-settings-organizations").addClass("selected");
    document.title = "Organizations";
  }
});
</script>

<style>

</style>
