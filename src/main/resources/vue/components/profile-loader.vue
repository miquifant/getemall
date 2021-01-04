<template id="profile-loader">
  <div v-if="error" class="alert alert-warning alert-dismissible fade in login-message" role="alert">
    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
    <i class="glyphicon glyphicon-warning-sign"></i>
    <span>{{ error }}</span>
  </div>
</template>
<script>
Vue.component("profile-loader", {
  template: "#profile-loader",
  props: [ "parent" ],
  data: () => ({
    error: null
  }),
  methods: {
    setProfile: function() {
      // Set Getemall profile from logged-in user
      handleFetch("profiles",
        "name/" + this.$javalin.state.currentUser,
        profile => this.parent.profile = profile,
        e => this.error = e.message);
    }
  },
  mounted: function() {
    if (this.parent) this.setProfile();
    else this.error = "Unable to retrieve profile information";
  }
});
</script>
