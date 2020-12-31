<template id="organization">
  <div class="organization-container">

    <div class="row">
      <div class="col-xs-2">
        <label>id</label>
      </div>
      <div class="col-xs-10">
        <input v-if="org"
               type="text"
               :value="org.id"
               disabled
               required/>
        <input v-else type="text" value="" disabled/>
      </div>
    </div>

    <div class="row">
      <div class="col-xs-2">
        <label>name</label>
      </div>
      <div class="col-xs-10">
        <input v-if="org"
               id="orgName"
               type="text"
               v-model="org.name"
               :value="org.name"
               placeholder="Organization name"
               :disabled="readonly"
               required/>
        <input v-else type="text" value="" disabled/>
      </div>
    </div>

    <div class="row">
      <div class="col-xs-2">
        <label>owner</label>
      </div>
      <div class="col-xs-10">
        <input v-if="org"
               type="text"
               v-model="org.owner"
               :value="org.owner"
               disabled
               required/>
        <input v-else type="text" value="" disabled/>
      </div>
    </div>

  </div>
</template>
<script>
Vue.component("organization", {
  // org: object with id, name and owner
  // readonly: will disable all fields of form
  props: ["org", "readonly"],
  template: "#organization",
});
</script>
<style>
.organization-container {
  padding: 3px;
}
.organization-container .row {
  margin: 10px 0px;
}
.organization-container .row input {
  width: 100%;
}
</style>
