<template id="profile">
  <app-frame>
    <div v-if="profile" class="row">
      <div class="col-sm-3">

        <!-- Avatar -->
        <div class="avatar">
          <img v-if="profile.ext.profilePic"
               class="img-circle avatar200 width-full"
               :src="'/avatars/' + profile.ext.profilePic"
               width="200" height="200" :alt="'@' + profile.name">
          <img v-else
               class="img-circle avatar200 width-full"
               src="/avatar.png"
               width="200" height="200" :alt="'@' + profile.name">
          <div class="role">
            <img v-if="profile.role === 1" src="/superuser.gif" width="40" height="40" title="admin user">
            <img v-else src="/0.gif" width="40" height="40">
          </div>
        </div>

        <!-- Names -->
        <h1 class="vcard-names">
          <div class="vcard-fullname">{{ profile.ext.fullName }}</div>
          <div class="vcard-username">{{ profile.name }}</div>
        </h1>

        <!-- Bio -->
        <div class="user-profile-bio">
          <div>{{ profile.ext.bio }}</div>
        </div>

        <!-- Button -->
        <div v-if="itsMe">
          <a class="btn btn-default btn-edit-profile" href="/settings/profile">Edit profile</a>
        </div>
        <div v-else class="row">
          <div class="col-sm-9 col-follow">
            <a class="btn btn-default" href="#" disabled>Follow</a>
          </div>
          <div class="col-sm-3 col-other-actions">
            <a class="btn btn-default" href="#" disabled>&hellip;</a>
          </div>
        </div>

        <!-- vCard details -->
        <ul class="vcard-details">
          <li v-if="profile.ext.homeLocation" class="vcard-detail">
            <i class="glyphicon glyphicon-map-marker"></i>
            <span>{{ profile.ext.homeLocation }}</span>
          </li>
          <li v-if="profile.ext.pubEmail" class="vcard-detail">
            <i class="glyphicon glyphicon-envelope"></i>
            <a :href="'mailto:' + profile.ext.pubEmail">{{ profile.ext.pubEmail }}</a>
            <i v-if="profile.ext.pubEmailVerified" class="glyphicon glyphicon-ok-sign green" title="verified"></i>
            <i v-else class="glyphicon glyphicon-remove red" title="unverified"></i>
          </li>
          <li v-if="profile.ext.websiteUrl" class="vcard-detail">
            <i class="glyphicon glyphicon-link"></i>
            <a :href="profile.ext.websiteUrl">{{ profile.ext.websiteUrl }}</a>
          </li>
        </ul>

      </div>

      <!-- page -->
      <div class="col-sm-9">
        <pre>{{ profile }}</pre>
      </div>

    </div>
    <div v-if="error" class="row">
      <div class="col-sm-12">
        <center>
          <h1><strong>404</strong></h1>
          <h2><small>Profile not found</small></h2>
        </center>
      </div>
    </div>
  </app-frame>
</template>

<script>
Vue.component("profile", {
  template: "#profile",
  data: () => ({
    itsMe: false,
    profile: null,
    error: null
  }),
  mounted: function() {
    $(".app-frame").css("max-width", "1000");
    this.itsMe = this.$javalin.pathParams["name"] === this.$javalin.state.currentUser;
    handleFetch("profiles",
      "name/" + this.$javalin.pathParams["name"],
      profile => {
        this.profile = profile;
        document.title = profile.name + ((profile.ext.fullName)? " (" + profile.ext.fullName + ")": "");
      },
      e => this.error = e.message);
  }
});
</script>

<style>
.avatar {
  position: relative;
}
.avatar200 {
  background-color: #eee;
  box-shadow: 0px 0px 0px 1px #eee;
  max-width: 240px;
  height: auto;
}
.width-full {
  width: 100%!important;
}
.role {
  position: absolute;
  top: 0px;
  left: 0px;
}
.vcard-names {
  line-height: 1;
  margin-bottom: 16px;
}
.vcard-fullname {
  font-size: 26px;
  line-height: 1.25;
}
.vcard-username {
  font-size: 20px;
  font-style: normal;
  font-weight: 300;
  line-height: 24px;
  color: #999;
}
.user-profile-bio {
  font-size: 14px;
  margin-bottom: 16px;
}
.btn-edit-profile {
  width: 100%;
}
.btn-follow {
  width: 100%;
}
.col-follow {
  padding-right: 8px;
}
.col-follow a {
  width: 100%;
}
.col-other-actions {
  padding-left: 0;
}
.col-other-actions a {
  width: 100%;
}
.vcard-details {
  margin-top: 16px;
  list-style: none;
  padding-left: 0px;
}
.vcard-detail {
  padding-top: 4px!important;
  padding-left: 0px;
  color: #24292e;
}
.vcard-detail i {
  position: relative;
  top: 3px;
  margin-right: 4px;
  color: #6a737d;
}
.vcard-detail a {
  color: #24292e;
}
pre {
  margin-top: 20px;
}
</style>
