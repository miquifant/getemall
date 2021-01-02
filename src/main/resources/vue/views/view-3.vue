<template id="view-3">
  <app-frame>
    <h1 class="title-view-3">This is the third view</h1>
    <h2>Only for admins</h2>
    <h3>Organizations</h3>

    <!-- organizations table with control buttons (+)(-) -->
    <div class="well well-sm row row-inside-well">
      <div class="btngroup col-xs-8">
        <button class="btn btn-success btn-sm"
                @click="createOrg"
                :disabled="userid === 0"
                title="Create a new organization"><i
                class="glyphicon glyphicon-plus"></i>
                <span>New</span>
        </button>
        <button class="btn btn-danger btn-sm"
                @click="deleteOrgs"
                :disabled="checkedOrgs.length === 0"
                title="Delete selected organizations"><i
                class="glyphicon glyphicon-trash"></i>
                <span>Delete</span>
        </button>
      </div>
      <div class="btngroup btngroup-right col-xs-4">
        <button class="btn btn-default"
                @click="refreshOrgs"
                title="Refresh list of organizations"><i
                class="glyphicon glyphicon-refresh"></i>
        </button>
      </div>
      <organizations-table :orgs="organizations" :parent="this"></organizations-table>
    </div>
    
    <div class="row">
      <div class="col-xs-4">
        <organizations-table :orgs="organizations"></organizations-table>
      </div>
    </div>

    <!-- Modal dialog edit Organization -->
    <div id="editOrg" ref="editOrg" class="modal fade" role="dialog" tabindex="-1" aria-labelledby="editOrgLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <form autocomplete="off" role="presentation" action="#" @submit.prevent="saveOrg">
            <div class="modal-header">
              <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
              <h4 class="modal-title editOrg-title">
                <i class="glyphicon glyphicon-edit"></i>
                <span id="editOrgLabel">Edit Organization</span>
              </h4>
            </div>
            <div class="modal-body">
              <organization v-if="editedOrg" :org="editedOrg" :parent="this"></organization>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
              <button type="submit" class="btn btn-primary">Save</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <confirm-danger id="confirmDeletion"
                    :message="'<h4>This action can\'t be undone.</h4>'                                     +
                              'Are you sure you want to <strong>DELETE</strong> following Organization?'   +
                              '<ul><li>' + ((this.orgToDelete)? this.orgToDelete.name: '') + '</li></ul>'"
                    action="<i class='glyphicon glyphicon-trash'></i>&nbsp; Delete"
                    :onaccept="doDeleteOrg"
                    :onhide="stopDeletingOrg"></confirm-danger>

    <confirm-danger id="confirmMultipleDeletion"
                    v-if="this.checkedOrgs.length > 0"
                    :message="'<h4>This action can\'t be undone.</h4>'                                     +
                              'Are you sure you want to <strong>DELETE</strong> following Organizations?'  +
                              '<ul>' + this.checkedOrgs.map(x => '<li>' + findOrgById(x).name + '</li>').join('') + '</ul>'"
                    action="<i class='glyphicon glyphicon-trash'></i>&nbsp; Delete"
                    :onaccept="doDeleteMultipleOrgs"></confirm-danger>

  </app-frame>
</template>

<script>
Vue.component("view-3", {
  template: "#view-3",
  data: () => ({
    userid: 0,
    organizations: [],
    checkedOrgs: [],
    editedOrg: null,
    orgToDelete: null
  }),
  methods: {
    findOrgById: function(id) {
      for (org of this.organizations) if (org.id == id) return org;
      return null;
    },
    createOrg: function() {
      this.editedOrg = { id: 0, name: "", owner: this.userid }
      $("#editOrg").modal({ backdrop: "static" });
    },
    deleteOrgs: function() {
      $("#confirmMultipleDeletion").modal({ backdrop: "static" });
    },
    editOrg: function(org) {
      this.editedOrg = JSON.parse(JSON.stringify(org));
      $("#editOrg").modal({ backdrop: "static" });
    },
    stopEditingOrg: function() {
      this.editedOrg = null;
    },
    deleteOrg: function(org) {
      this.orgToDelete = org;
      $("#confirmDeletion").modal({ backdrop: "static" });
    },
    stopDeletingOrg: function() {
      this.orgToDelete = null;
    },
    // --------------------------------------------------------------------------------------------
    // API fetching functions
    // --------------------------------------------------------------------------------------------
    refreshOrgs: function() {
      handleFetchList("organizations", list => this.organizations = list);
    },
    setProfileId: function() {
      // Set Getemall profile from logged-in user
      handleFetch("profiles",
        "name/" + this.$javalin.state.currentUser,
        profile => this.userid = profile.id,
        e => this.userid = 0);
    },
    doDeleteMultipleOrgs: function() {
      this.checkedOrgs.forEach(id => {
          handleDelete("organizations", id, (deletedOrg) => {
              this.checkedOrgs.splice(0, 1);
              this.refreshOrgs();
          });
      });
      $("#confirmMultipleDeletion").modal("hide");
    },
    doDeleteOrg: function() {
      handleDelete("organizations", this.orgToDelete.id, (deletedOrg) => {
          this.refreshOrgs();
          $("#confirmDeletion").modal("hide");
      });
    },
    saveOrg: function() {
      if (this.editedOrg.id === 0) {
        handleSave("organizations", this.editedOrg, (org) => {
            this.editedOrg = org;
            this.refreshOrgs();
            $("#editOrg").modal("hide");
        });
      }
      else {
        org = this.findOrgById(this.editedOrg.id)
        if (JSON.stringify(org) !== JSON.stringify(this.editedOrg)) {
          handleSave("organizations", this.editedOrg, (org) => {
              this.editedOrg = org;
              this.refreshOrgs();
              $("#editOrg").modal("hide");
          });
        }
        else $("#editOrg").modal("hide");
      }
    }
  },
  mounted: function() {
    $("#v3-tab").tab("show")
    $(this.$refs.editOrg).on("shown.bs.modal", () => $("#orgName").focus());
    // Solution by Bert Evans https://stackoverflow.com/questions/42512770/handle-bootstrap-modal-hide-event-in-vue-js
    $(this.$refs.editOrg).on("hidden.bs.modal", this.stopEditingOrg);

    this.refreshOrgs();
    this.setProfileId();
  }
});
</script>

<style>
.title-view-3 {
  color: red;
}
.btngroup {
  padding: 0px;
  margin-bottom: 10px;
}
.btngroup-right {
  text-align: right;
}
.row-inside-well {
  margin-left: 0px;
  margin-right: 0px;
}
button span {
  margin-left: 6px;
}
.editOrg-title span {
 margin-left: 6px;
}
#confirmDeletion ul,
#confirmMultipleDeletion ul {
  list-style-type: none;
  margin: 20px;
  padding: 0;
}
#confirmDeletion li,
#confirmMultipleDeletion li {
  border: solid 1px #a94442;
  padding: 4px 10px;
  border-radius: 2px;
  margin-bottom: 1px;
}
</style>
