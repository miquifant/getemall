<template id="organizations-table">
  <table class="organizations-table">
    <thead>
      <tr>
        <th v-if="parent && parent.checkedOrgs">&nbsp;</th>
        <th>id</th>
        <th>name</th>
        <th>owner</th>
        <th v-if="parent && (parent.editOrg || parent.deleteOrg)">&nbsp;</th>
      </tr>
    </thead>
    <tbody v-if="orgs.length > 0">
      <tr v-for="org in orgs" @dblclick="if (parent && parent.editOrg) parent.editOrg(org)">
        <td v-if="parent && parent.checkedOrgs" class="col-select">
          <input type="checkbox"
                 :value="org.id"
                 v-model="parent.checkedOrgs">
        </td>
        <td class="col-id">{{ org.id }}</td>
        <td class="col-name">{{ org.name }}</td>
        <td class="col-owner">{{ org.owner }}</td>
        <td v-if="parent && (parent.editOrg || parent.deleteOrg)"
            class="col-actions">
          <button v-if="parent && parent.editOrg"
                  class="actn"
                  @click="parent.editOrg(org)"
                  title="Edit"><i
                  class="glyphicon glyphicon-pencil"></i></button>
          <button v-if="parent && parent.deleteOrg"
                  class="actn delete"
                  @click="parent.deleteOrg(org)"
                  title="Delete"><i
                  class="glyphicon glyphicon-trash"></i></button>
        </td>
      </tr>
    </tbody>
    <tbody v-else>
      <tr><td colspan="5" align="center">There are no records to show at this moment</td></tr>
    </tbody>
  </table>
</template>
<script>
Vue.component("organizations-table", {
  // orgs: array of objects with id, name and owner
  // parent: if parent has...
  // > data.checkedOrgs: []   => a checkbox will be enabled for every row
  // > methods.editOrg(org)   => an edit button will be enabled for every row
  // > methods.deleteOrg(org) => a delete button will be enabled for every row
  props: ["orgs", "parent"],
  template: "#organizations-table"
});
</script>
<style>
.organizations-table {
  border-radius: 3px;
  background: white;
  width: 100%;
  border-spacing: 0px;
  color: #666;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.25);
}
.organizations-table tr > th:first-child {
  border-top-left-radius: 3px;
}
.organizations-table tr > th:last-child {
  border-top-right-radius: 3px;
}
.organizations-table tr:hover {
  color: #06324f;
  background-color: #ffaa0050;
}
.organizations-table tr:last-child:hover td:first-child {
  border-bottom-left-radius: 3px;
}
.organizations-table tr:last-child:hover td:last-child {
  border-bottom-right-radius: 3px;
}
.organizations-table th {
  text-align:center;
  font-size: 8pt;
  background-color: #e0e0f0;
  color: #666;
  padding-top: 4px;
  padding-bottom: 4px;
  border-bottom: solid 1px #999;
}
.organizations-table td {
  font-size: 8pt;
  padding-top: 4px;
  padding-bottom: 4px;
}
.col-select {
  width: 20px;
  padding-left: 4px;
}
.col-id {
  text-align: right;
  padding-right: 8px;
  width: 40px;
}
.col-name {
  width: auto;
  padding-left: 8px;
}
.col-owner {
  text-align: right;
  padding-right: 8px;
  width: 40px;
}
.col-actions {
  width: 60px;
  text-align: center;
}
.organizations-table input[type="checkbox"] {
  cursor: pointer;
}
</style>
