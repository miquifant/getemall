<template id="settings-password">
  <settings>
    <profile-loader :parent="this"></profile-loader>
    <h2 class="settings-title">Change password</h2>
    <form v-if="profile" id="settingsProfileForm" method="post">
      <div class="row">
        <div class="col-sm-8">
          <div class="form-group">
            <label for="oldpassword">Old password</label>
            <input type="password"
                   class="form-control"
                   id="oldpassword" name="oldpassword"
                   autocomplete="current-password"
                   required>
          </div>
          <div class="form-group">
            <label for="newpassword">New password</label>
            <input type="password"
                   class="form-control"
                   id="newpassword" name="newpassword"
                   autocomplete="off"
                   required>
          </div>
          <div class="form-group">
            <label for="confirmpassword">Confirm new password</label>
            <input type="password"
                   class="form-control"
                   id="confirmpassword" name="confirmpassword"
                   autocomplete="new-password"
                   required>
          </div>
        </div>
      </div>
      <button type="submit" class="btn btn-default" disabled>Update password</button>
    </form>
  </settings>
</template>

<script>
Vue.component("settings-password", {
  template: "#settings-password",
  data: () => ({
    profile: null
  }),
  mounted: function() {
    $("#menu-settings-password").addClass("selected");
    document.title = "Account password";
  }
});
</script>

<style>

</style>
