<template id="confirm-danger">
  <div class="modal" :id="id" :ref="id" role="dialog" :aria-labelledby="'__' + id + '_Label'" tabindex="-1">
    <div class="modal-dialog confirm-danger-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header confirm-danger-header">
          <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
          <h4 class="modal-title confirm-danger-title">
            <i class="glyphicon glyphicon-exclamation-sign"></i>
            <span :id="'__' + id + '_Label'">Warning! Destructive command</span>
          </h4>
        </div>
        <div class="modal-body" v-html="message"></div>
        <div class="modal-footer">
          <button type="button" class="btn btn-default"
                  :id="'cancel_' + id"
                  data-dismiss="modal">No</button>
          <button type="button" class="btn btn-danger"
                  @click="__onAccept"
                  :disabled="!onaccept"
                  v-html="action? action: 'Accept'"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
Vue.component("confirm-danger", {
  // id: for showing and hiding: $("#<THE_ID>").modal({backdrop:"static"}); and $("#<THE_ID>").modal("hide");
  // message: threatening message to scare you to death
  // action: text of the red button (i.e. "Delete")
  // onaccept: function to execute on accept. Mandatory or the accept button will be disabled
  // onhide: (optional) function to execute when the dialog is hidden
  props: [ "id", "message", "action", "onaccept", "onhide" ],
  template: "#confirm-danger",
  methods: {
    __onAccept: function() {
      if (this.onaccept) this.onaccept();
      else {
        alert("Honestly, I don't know what to do now. Sorry");
        $("#" + this.id).modal("hide");
      }
    },
    __onHide: function() {
      if (this.onhide) this.onhide();
    }
  },
  mounted: function() {
    $(this.$refs[this.id]).on("shown.bs.modal", () => $("#cancel_" + this.id).focus());
    $(this.$refs[this.id]).on("hidden.bs.modal", this.__onHide);
  }
});
</script>

<style>
.confirm-danger-dialog {
  margin-top: 180px;
}
.confirm-danger-header {
  border-top-left-radius: inherit;
  border-top-right-radius: inherit;
  color: #a94442;
  background: #f2dede;
  border-bottom: #ebccd1;
}
.confirm-danger-title span {
  margin-left: 6px;
}
.confirm-danger-header .close {
  color: inherit;
}
</style>
