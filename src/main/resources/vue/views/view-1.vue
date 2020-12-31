<template id="view-1">
  <app-frame>
    <h1 class="title-view-1">This is the first view</h1>
    <h2 class="sub-title-view-1">Anyone can see it</h2>
    <h3>[ Demo  of <code>confirm-danger</code> modal ]</h3>
    <div class="somebuttons">
      <div class="row">
        <div class="col-sm-2">
          <button class="btn btn-danger" @click="confirmDangerousAction('uno')">One</button>
        </div>
        <div class="col-sm-10"><h4>plain text message, default button, no action on hide</h4></div>
      </div>
      <div class="row">
        <div class="col-sm-2">
          <button class="btn btn-danger" @click="confirmDangerousAction('dos')">Two</button>
        </div>
        <div class="col-sm-10"><h4>formatted message, custom button, log on hide</h4></div>
      </div>
      <div class="row">
        <div class="col-sm-2">
          <button class="btn btn-danger" @click="confirmDangerousAction('tres')">Three</button>
        </div>
        <div class="col-sm-10"><h4>formatted message, custom button with icon, log on hide</h4></div>
      </div>
      <center>
        <button class="btn btn-default normal"
                @click="backToNormal"
                :disabled="normal">Back to normal</button>
      </center>
    </div>

    <confirm-danger id="uno"
                    message="Are you sure of action one?"
                    :onaccept="action1"></confirm-danger>

    <confirm-danger id="dos"
                    message="<h1>Oh my God</h1>Are you really gonna do action 2?"
                    :onaccept="action2"
                    :onhide="() => console.log('end of management of second action')"
                    action="No fear!"></confirm-danger>

    <confirm-danger id="tres"
                    message="Are you sure you want <em>that</em>?<br><small>(do it at your own risk)</small>"
                    :onaccept="action3"
                    :onhide="() => console.log('third action cleanup')"
                    action="<i class='glyphicon glyphicon-flash'></i> Kill me!"></confirm-danger>

  </app-frame>
</template>

<script>
Vue.component("view-1", {
  template: "#view-1",
  data: () => ({
    normal: true
  }),
  methods: {
    confirmDangerousAction: function(id) {
      console.log("Do here some initializations before showing danger dialog #" + id);
      $("#" + id).modal({ backdrop: "static" });
    },
    action1: function() {
      $(".title-view-1").text("You are brave").css("color", "#f0c");
      this.normal = false;
      $("#uno").modal("hide");
    },
    action2: function() {
      $(".sub-title-view-1").text("You don't know when to stop").css("color", "#f0c");
      this.normal = false;
      $("#dos").modal("hide");
    },
    action3: function() {
      $(".title-view-1").text("Thank God there's no fourth button").css("color", "black");
      $(".sub-title-view-1").text("You would have destroyed the Universe").css("color", "red");
      this.normal = false;
      $("#tres").modal("hide");
    },
    backToNormal: function() {
      $(".title-view-1").text("This is the first view").css("color", "blue");
      $(".sub-title-view-1").text("Anyone can see it").css("color", "#333");
      this.normal = true;
    }
  },
  mounted: function() {
    $("#v1-tab").tab("show")
  }
});
</script>

<style>
.title-view-1 {
  color: blue;
}
.somebuttons {
  max-width: 700px;
  margin: 40px auto;
  border: dotted 1px silver;
  border-radius: 10px;
  padding: 20px;
}
.somebuttons .row {
  margin-top: 6px;
}
.somebuttons .col-sm-2 {
  text-align: right;
}
.normal {
  margin-top: 50px;
}
</style>
