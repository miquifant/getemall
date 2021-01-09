<template id="settings">
  <app-frame>
    <profile-loader :parent="this"></profile-loader>
    <div class="row">
      <div class="col-sm-3">
        <nav class="menu">
          <div class="menu-heading">
            <img v-if="profile && profile.ext.profilePic"
                 class="img-circle avatar32"
                 :src="'/avatars/' + profile.ext.profilePic"
                 width="32" height="32" :alt="'@' + profile.name">
            <img v-else
                 class="img-circle avatar32"
                 src="/avatar.png"
                 width="32" height="32">
            <div>
              <div v-if="profile" class="menu-head-title">{{ profile.name }}</div>
              <div v-else class="menu-head-title">&nbsp;</div>
              <div class="menu-head-subtitle">Personal settings</div>
            </div>
          </div>
          <a id="menu-settings-account" class="menu-item" href="/settings/account">Account</a>
        </nav>
      </div>
      <div class="col-sm-9">
        <slot></slot>
      </div>
    </div>
  </app-frame>
</template>

<script>
Vue.component("settings", {
  template: "#settings",
  data: () => ({
    profile: null
  }),
  mounted: function() {
    $("#profile-tab").tab("show");
    $(".app-frame").css("max-width", "1000");
  }
});
</script>

<style>
.menu {
  margin-bottom: 16px;
  list-style: none;
  border: 1px solid silver;
  border-radius: 6px;
}
.menu-heading {
  padding: 8px 16px;
  margin-top: 0;
  margin-bottom: 0;
  font-size: inherit;
  border-bottom: 1px solid silver;
}
.menu-heading:first-child {
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
}
.avatar32 {
  background-color: #eee;
  box-shadow: 0px 0px 0px 1px #eee;
  float: left;
  margin-right: 8px;
}
.menu-head-title {
  font-weight: bold;
}
.menu-head-subtitle {
  color: grey;
  font-size: .9em;
  line-height: 12px;
}
.menu-item {
  position: relative;
  display: block;
  padding: 8px 16px;
  border-bottom: 1px solid silver;
  margin-bottom: 0px;
}
.menu-item:hover,
.menu-item:active,
.menu-item:focus {
  background: #f6f8fa;
  text-decoration: none;
}
.menu-item:last-child {
  border-bottom-left-radius: 5px;
  border-bottom-right-radius: 5px;
  border-bottom: none;
}
.menu-item:hover:last-child {
  border-bottom-left-radius: 5px;
  border-bottom-right-radius: 5px;
}
.selected {
  color: #333;
  border-left: solid 2px #f9826c;
  cursor: default;
}
.selected:hover {
  background: none;
  color: #333;
}
.settings-title {
  margin-top: 0px;
  border-bottom: solid 1px #eee;
  padding-bottom: 8px;
}
.settings-title-danger {
  color: #cb2431;
}
.subhead-spacious {
  margin-top: 40px;
}
.settings-comment {
  margin-top: 10px;
  margin-bottom: 10px;
}
.settings-comment-small {
  font-size: .9em;
  color: #666;
}
</style>
