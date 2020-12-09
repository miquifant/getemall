    <template id="login">
      <app-frame>
        <form id="loginForm" method="post">
          <p v-if="loginState.authError" class="verybad notification">Can't log in at the moment. We are having problems.</p>
          <p v-if="loginState.authFailed" class="bad notification">The login information you supplied was incorrect.</p>
          <p v-if="loginState.authSucceeded" class="good notification">Authentication Succeeded.</p>
          <p v-if="loginState.loggedOut" class="notification">You have been logged out.</p>
          <h1>Login</h1>
          <label>Username</label>
          <input type="text" name="username" placeholder="username" value="" required>
          <label>Password</label>
          <input type="password" name="password" placeholder="password" value="" required>
          <input v-if="loginState.redirect" type="hidden" name="redirect" :value="loginState.redirect">
          <input type="submit" value="Log in">
          <span v-if="currentUser">You're already logged in as <strong>{{ this.currentUser }}</strong></span>
          <span v-if="loginState.redirect">You need to be logged in to see that page.</span>
        </form>
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
            .catch(() => Console.log("Error while fetching login state"));
        }
      });
    </script>
    <style>
      #loginForm {
        max-width: 400px;
        margin: 0 auto;
      }
      #loginForm label {
        display: block;
        width: 100%
      }
      #loginForm input {
        border: 1px solid #ddd;
        padding: 8px 12px;
        width: 100%;
        border-radius: 3px;
        margin: 2px 0 20px 0;
      }
      #loginForm input[type="submit"] {
        color: white;
        background: #274555;
        border: 0;
        cursor: pointer;
      }
      .notification {
        padding: 10px;
        background: #333;
        color: white;
        border-radius: 3px;
      }
      .good.notification {
        background: #008900;
      }
      .bad.notification {
        background: #bb0000;
      }
      .verybad.notification {
        background: white;
        color: red;
        border: solid 3px red;
      }
    </style>
