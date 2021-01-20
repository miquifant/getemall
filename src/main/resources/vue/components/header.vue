<template id="header">
  <div class="header">
    <a href="/index"><img id="logo" src="/images/logo_100w.png" alt="Getemall"></a>
    <ul id="mytabs" class="nav nav-tabs">
      <li id="home-tab" role="presentation">
        <a href="/index" title="Home"><strong>Home</strong></a>
      </li>
      <li id="v1-tab" role="presentation">
        <a href="/v1" title="Public content">View 1</a>
      </li>
      <li id="v2-tab" role="presentation" :class="[$javalin.state.currentUser? '': 'disabled']">
        <a v-if="$javalin.state.currentUser" href="/v2" title="Logged in users only">View 2</a>
        <a v-else title="Logged in users only">View 2</a>
      </li>
      <li id="v3-tab" role="presentation" :class="[$javalin.state.currentRole === 'ADMIN'? '': 'disabled']">
        <a v-if="$javalin.state.currentRole === 'ADMIN'" href="/v3" title="Admins only">View 3</a>
        <a v-else title="Admins only">View 3</a>
      </li>
      <li id="v4-tab" role="presentation">
        <a href="/v4" title="You will not find it">View 4</a>
      </li>
      <!-- profile menu or log in -->
      <li v-if="!$javalin.state.currentUser" id="profile-tab" role="presentation">
        <a href="/login">Log in</a>
      </li>
      <li v-if="$javalin.state.currentUser" id="profile-tab" role="presentation" class="dropdown">
        <a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false">
          {{ $javalin.state.currentUser }} <span class="caret"></span>
        </a>
        <ul class="dropdown-menu" aria-labelledby="profileDropdownMenu">
          <li><a :href="'/profiles/' + $javalin.state.currentUser">Your profile</a></li>
          <li role="separator" class="divider"></li>
          <li><a href="/settings/profile">Settings</a></li>
          <li><a href="/logout" @click.prevent="$('#logout').submit()">Log out</a><form id="logout" method="post" action="/logout"></form></li>
        </ul>
      </li>
    </ul>
  </div>
</template>

<script>
Vue.component("page-header", {
  template: "#header"
});
</script>

<style>
.header {
  background: #24292e;
  color: white;
}
.header a {
  color: #bbb;
}
.header li a:hover,
.header li a:active,
.header li a:focus {
  color: #23527c;
}
.nav li.disabled a:hover {
  border-color: transparent;
}
#logo {
  max-height: 40px;
  margin: 10px;
}
</style>
