<template id="settings-account">
  <settings>
    <profile-loader :parent="this"></profile-loader>
    <h2 class="settings-title">Change username</h2>
    <form v-if="profile" id="settingsAccountForm" autocomplete="off" @submit.prevent="changeUsername">
      <div class="row">
        <div class="col-sm-8">
          <div class="form-group">
            <label for="curusername">Current username</label>
            <input type="text"
                   class="form-control"
                   id="curusername" name="curusername"
                   :value="profile.name" readonly>
          </div>
          <div class="form-group has-feedback">
            <label for="username">New username</label>
            <input type="text"
                   class="form-control"
                   id="username" name="username"
                   @input="checkUsernameWithDelay(600)"
                   placeholder="Choose a new username"
                   spellcheck="false" autocapitalize="off"
                   autofocus>
            <i id="feedback-username" class="form-control-feedback glyphicon glyphicon-ok"></i>
            <div v-if="badUsernameMsg" class="check-username-note" v-html="badUsernameMsg"></div>
          </div>
        </div>
      </div>
      <button type="submit" class="btn btn-default" :disabled="!validUsername">Change username</button>
    </form>
  </settings>
</template>

<script>
Vue.component("settings-account", {
  template: "#settings-account",
  data: () => ({
    profile: null,
    validUsername: false,
    badUsernameMsg: null,
    feedbackTimeout: null
  }),
  methods: {
    checkUsernameWithDelay: function(delay) {
      this.feedbackWait();
      if (this.feedbackTimeout) clearTimeout(this.feedbackTimeout);
      this.feedbackTimeout = setTimeout(this.checkUsername, delay);
    },
    checkUsername: function() {
      let username = $("#username").val();
      let unchanged = (username && username.toLowerCase() === this.profile.name.toLowerCase());
      // Offline validation
      if (!username) this.feedbackStop();
      else if (unchanged) this.feedbackFail("Choose a new username");
      else if (username.length == 1) this.feedbackFail("Username is too short (minimum is 2 characters).");
      else if (username.length > 40) this.feedbackFail("Username is too long (maximum is 40 characters).");
      else if (!/^[a-z0-9]([a-z0-9]|[-]){0,38}[a-z0-9]$/i.test(username) || /[-][-]/.test(username))
        this.feedbackFail("Username may only contain alphanumeric characters or single hyphens, " +
                          "and cannot begin or end with a hyphen.");
      // Online: check availability of username
      else handleCheck("profiles", "name/" + $("#username").val(),
          exists => {
            if (!exists) this.feedbackSucceed();
            else this.feedbackFail("Username " + username + " is not available. Please choose another.");
          },
          e => this.feedbackFail("We are having problems. It may not be possible to change username now."));
    },
    feedbackFail: function(msg) {
      this.badUsernameMsg = msg;
      this.validUsername = (this.badUsernameMsg == null);
      $("#username").addClass("form-control-danger");
      $("#feedback-username").show()
        .removeClass("loader")
        .removeClass("glyphicon-ok")
        .addClass("glyphicon-warning-sign");
    },
    feedbackSucceed: function() {
      this.badUsernameMsg = null;
      this.validUsername = true;
      $("#username").removeClass("form-control-danger");
      $("#feedback-username").show()
        .removeClass("loader")
        .removeClass("glyphicon-warning-sign")
        .addClass("glyphicon-ok");
    },
    feedbackStop: function() {
      this.badUsernameMsg = null;
      this.validUsername = false;
      $("#username").removeClass("form-control-danger");
      $("#feedback-username").hide()
        .removeClass("glyphicon-ok")
        .removeClass("glyphicon-warning-sign");
    },
    feedbackWait: function() {
      this.feedbackStop();
      $("#feedback-username").show()
        .addClass("loader");
    },
    // --------------------------------------------------------------------------------------------
    // API fetching functions
    // --------------------------------------------------------------------------------------------
    changeUsername: function() {
      handlePatch("profiles", "me", {
          name: $("#username").val()
        },
        () => location.reload(),
        e => this.feedbackFail(e.message));
    }
  },
  mounted: function() {
    $("#menu-settings-account").addClass("selected");
    document.title = "Account settings";
  }
});
</script>

<style>
.avatar {
  position: relative;
  margin-top: 6px;
}
.avatar200 {
  background-color: #eee;
  box-shadow: 0px 0px 0px 1px #eee;
}
.role {
  position: absolute;
  top: 0px;
  left: 0px;
}
.btn-edit-avatar {
  position: absolute;
  left: 0px;
  bottom: 0px;
  margin: 10px 6px;
  padding: 4px 8px;
}
.btn-edit-avatar span {
  margin-left: 4px;
}
.form-control-danger {
  border-color: #a94442;
}
.form-control-danger:focus {
  border-color: #a94442;
  outline: 0;
  -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075), 0 0 8px rgba(233,102,102,.6);
  box-shadow: inset 0 1px 1px rgba(0,0,0,.075), 0 0 8px rgba(233,102,102,.6);
}
.form-control-feedback {
  display: none;
}
#feedback-username.glyphicon-ok {
  color: #5ab034;
}
#feedback-username.glyphicon-warning-sign {
  color: #bf1515;
}
.loader {
  border-radius: 50%;
  border-top: 3px solid #66afe9cc;
  border-right: 3px solid #66afe933;
  border-bottom: 3px solid #66afe966;
  border-left: 3px solid #66afe999;
  width: 14px;
  height: 14px;
  -webkit-animation: spin 600ms linear infinite;
  animation: spin 600ms linear infinite;
}
@-webkit-keyframes spin {
  0% { -webkit-transform: rotate(0deg); }
  100% { -webkit-transform: rotate(360deg); }
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
#feedback-username.loader {
  right: 10px;
  margin-top: 10px;
}
.check-username-note {
  min-height: 17px;
  margin: 0 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #a94442;
}
</style>
