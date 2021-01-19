<template id="settings-profile">
  <settings>
    <profile-loader :parent="this"></profile-loader>
    <h2 class="settings-title">Public profile</h2>
    <form v-if="profile" id="settingsProfileForm" action="#" @submit.prevent="saveProfile">
      <div class="row">
        <div class="col-sm-8">
          <div class="form-group">
            <label for="fullname">Name</label>
            <input type="text"
                   class="form-control"
                   id="fullname" name="fullname" placeholder="Full name"
                   pattern=".{1,128}" title="Full name (max length is 128 chars)"
                   spellcheck="false"
                   @input="checkChanges"
                   v-model="profile.ext.fullName"
                   autofocus>
          </div>
          <div class="form-group">
            <label for="email">Email</label>
            <input type="email"
                   class="form-control"
                   id="email" name="email" placeholder="Email address"
                   pattern=".{1,128}" title="Email address (max length is 128 chars)"
                   spellcheck="false" autocapitalize="off"
                   @input="checkChanges"
                   v-model="profile.ext.pubEmail">
            <p v-show="pubEmailVerified" class="settings-comment settings-comment-small">
              <i class="glyphicon glyphicon-ok-sign green" title="verified"></i>
              <span>This email is verified!</span>
            </p>
            <p v-show="!pubEmailVerified" class="settings-comment settings-comment-small">
              <i class="glyphicon glyphicon-remove red" title="unverified"></i>
              <span>It won't be published until it's verified</span>
            </p>
          </div>
          <div class="form-group">
            <label for="bio">Bio</label>
            <textarea class="form-control"
                      id="bio" name="bio" placeholder="Tell us a little about yourself"
                      pattern=".{1,256}" title="Biography (max length is 256 chars)"
                      @input="checkChanges"
                      v-model="profile.ext.bio"></textarea>
          </div>
          <div v-if="error" class="alert alert-warning alert-dismissible fade in updErr" role="alert">
            <button type="button" class="close" aria-label="Close" @click="$('.updErr').hide()"><span aria-hidden="true">&times;</span></button>
            <i class="glyphicon glyphicon-warning-sign"></i>
            <span v-html="error"></span>
          </div>
        </div>
        <div class="col-sm-4">
          <label for="avatar">Profile picture</label>
          <div class="avatar">
            <img v-if="profile.ext.profilePic"
                 class="img-circle avatar200"
                 :src="'/avatars/' + profile.ext.profilePic"
                 width="200" height="200" :alt="'@' + profile.name">
            <img v-else
                 class="img-circle avatar200"
                 src="/avatar.png"
                 width="200" height="200" :alt="'@' + profile.name">
            <div class="role">
              <img v-if="profile.role === 1" src="/superuser.gif" width="40" height="40" title="admin user">
              <img v-else src="/0.gif" width="40" height="40">
            </div>
            <button class="btn btn-default btn-edit-avatar" disabled>
              <i class="glyphicon glyphicon-pencil"></i>
              <span>Edit</span>
            </button>
          </div>
        </div>
      </div>
      <button type="submit" class="btn btn-success" :disabled="disableSave">Update profile</button>
    </form>
  </settings>
</template>

<script>
Vue.component("settings-profile", {
  template: "#settings-profile",
  data: () => ({
    profile: null,
    previous: null,
    disableSave: true,
    error: null
  }),
  methods: {
    onProfileLoaded: function() {
      this.previous = JSON.parse(JSON.stringify(this.profile));
    },
    checkChanges: function() {
      this.disableSave = (
        this.profile.ext.fullName == this.previous.ext.fullName &&
        this.profile.ext.pubEmail == this.previous.ext.pubEmail &&
        this.profile.ext.bio      == this.previous.ext.bio
      );
    },
    errorOcurred: function(e) {
      this.error = e.replace(/\n/g, '<br/>');
      $('.updErr').show();
    },
    // --------------------------------------------------------------------------------------------
    // API fetching functions
    // --------------------------------------------------------------------------------------------
    saveProfile: function() {
      handleUpsert("profiles/me/ext", this.profile.ext,
        () => location.reload(),
        e => this.errorOcurred(e.message));
    }
  },
  computed: {
    pubEmailVerified: function() {
      return (
        this.profile.ext.pubEmailVerified &&
        this.profile.ext.pubEmail == this.previous.ext.pubEmail
        ||
        this.profile.verified &&
        this.profile.ext.pubEmail == this.profile.email);
    }
  },
  mounted: function() {
    $("#menu-settings-profile").addClass("selected");
    document.title = "Your profile";
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
</style>
