<template id="view-2">
  <app-frame>
    <h1 class="title-view-2">This is the second view</h1>
    <h2>Only for logged in users (like you, <strong>{{ $javalin.state.currentUser }}</strong>)</h2>
  </app-frame>
</template>

<script>
  Vue.component("view-2", {
    template: "#view-2",
    mounted: function() {
      $("#v2-tab").tab("show")
    }
  });
</script>

<style>
.title-view-2 {
  color: orange;
}
</style>
