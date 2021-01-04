<template id="view-2">
  <app-frame>
    <profile-loader :parent="this"></profile-loader>
    <h1 class="title-view-2">This is the second view</h1>
    <h2>Only for logged in users (like you, <strong>{{ $javalin.state.currentUser }}</strong>)</h2>
    <div>
      <pre v-if="profile">{{ profile }}</pre>
      <pre v-else>Profile info not found</pre>
    </div>
  </app-frame>
</template>

<script>
Vue.component("view-2", {
  template: "#view-2",
  data: () => ({
    profile: null
  }),
  mounted: function() {
    $("#v2-tab").tab("show");
  }
});
</script>

<style>
.title-view-2 {
  color: orange;
}
pre {
  margin-top: 20px;
}
</style>
