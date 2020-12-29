<template id="view-2">
  <app-frame>
    <h1 class="title-view-2">This is the second view</h1>
    <h2>Only for logged in users (like you, <strong>{{ $javalin.state.currentUser }}</strong>)</h2>
    <div>
      <pre v-if="profile">{{ profile }}</pre>
      <pre v-else>{{ error }}</pre>
    </div>
  </app-frame>
</template>

<script>
Vue.component("view-2", {
  template: "#view-2",
  data: () => ({
    profile: null,
    error: null
  }),
  methods: {
    setProfile: function() {
      // Set Getemall profile from logged-in user
      handleFetch("profiles",
        "name/" + this.$javalin.state.currentUser,
        profile => this.profile = profile,
        e => this.error = e.message);
    }
  },
  mounted: function() {
    $("#v2-tab").tab("show");
    this.setProfile();
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
