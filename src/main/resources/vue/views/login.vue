<template id="login">
  <app-frame>

    <div v-if="loginState.authError" class="alert alert-warning alert-dismissible fade in login-message" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
      <i class="glyphicon glyphicon-warning-sign"></i>
      <span>
        <strong>Oh no!</strong>
        Can't log in at the moment. We're having problems.
      </span>
    </div>

    <div v-if="loginState.authFailed" class="alert alert-danger alert-dismissible fade in login-message" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
      <i class="glyphicon glyphicon-exclamation-sign"></i>
      <span>
        <strong>Error!</strong>
        The login information you supplied was incorrect.
      </span>
    </div>

    <div v-if="loginState.authSucceeded" class="alert alert-success alert-dismissible fade in login-message" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
      <i class="glyphicon glyphicon-check"></i>
      <span>
        <strong>Hello!</strong>
        Authentication Succeeded.
      </span>
    </div>

    <div v-if="loginState.loggedOut" class="alert alert-info alert-dismissible fade in login-message" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
      <i class="glyphicon glyphicon-info-sign"></i>
      <span>
        <strong>Bye!</strong>
        You have been logged out.
      </span>
    </div>

    <div class="login">
      <h1>Login</h1>
      <form id="loginForm" method="post">
        <div class="form-group">
          <label for="username">User name:</label>
          <input type="username" class="form-control" id="username" name="username" placeholder="username" value="" required>
        </div>
        <div class="form-group">
          <label for="pwd">Password:</label>
          <input type="password" class="form-control" id="pwd" name="password" placeholder="password" value="" required>
        </div>
        <input v-if="loginState.redirect" type="hidden" name="redirect" :value="loginState.redirect">
        <button type="submit" class="btn btn-default">Submit</button>
      </form>
      <span class="bottom-message" v-if="currentUser">You're already logged in as <strong>{{ this.currentUser }}</strong></span>
      <span class="bottom-message" v-if="loginState.redirect">You need to be logged in to see that page.</span>
    </div>
  </app-frame>
</template>

<script>
Vue.component("login", {
  template: "#login",
  data: () => ({
    currentUser: null,
    loginState: null,
  }),
  created() {
    this.currentUser = this.$javalin.state.currentUser;
    fetch("/api/admin/loginState")
      .then(res => res.json())
      .then(res => this.loginState = res)
      .catch(() => console.log("Error while fetching login state"));
  },
  mounted: function() {
    $("#profile-tab").tab("show")
  }
});
</script>

<style>
.login-message {
  max-width: 500px;
  margin: 0 auto 20px auto;
}
.login-message strong {
 margin: 0 6px;
}
.login {
  max-width: 400px;
  margin: 0 auto;
  border: dotted 1px silver;
  border-radius: 6px;
  padding: 0 20px 20px 20px;
}
.bottom-message {
  display: block;
  margin-top: 30px;
}
</style>
