<template id="footer">
  <div class="footer">
    Getemall vers. <strong>{{ metadata.version }}</strong>.
    <button type="button" class="btn btn-dark btn-sm" data-toggle="modal" data-target="#metadata">more info</button>

    <!-- More info modal -->
    <div id="metadata" ref="metadata" class="modal fade" role="dialog" tabindex="-1" aria-labelledby="metadataLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="metadataLabel">About Getemall</h4>
          </div>
          <div class="modal-body modal-body-metadata">
            <div class="metadata">
              Getemall version {{ metadata.version }} ({{ metadata.date }})<br>
              Compiled by {{ metadata.user }}@{{ metadata.machine }}
            </div>
            <h4>Getemall uses</h4>
            <ul>
              <li><a href="https://gradle.org/" target="_blank">Gradle 5.6.4</a></li>
              <li><a href="https://kotlinlang.org/" target="_blank">Kotlin</a></li>
              <li><a href="https://javalin.io/" target="_blank">Javalin 3.11.0</a></li>
              <li><a href="https://vuejs.org/" target="_blank">Vue.js 2.6.10</a></li>
              <li><a href="https://jquery.com/" target="_blank">jQuery 1.11.1</a></li>
              <li><a href="https://getbootstrap.com/docs/3.4/" target="_blank">Bootstrap 3.4.1</a></li>
              <li><a href="https://www.glyphicons.com/" target="_blank">glyphicons</a></li>
            </ul>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" data-dismiss="modal">Ok</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
Vue.component("page-footer", {
  template: "#footer",
  data: () => ({
    metadata: null,
  }),
  created() {
    fetch("/api/admin/metadata")
      .then(res => res.json())
      .then(res => this.metadata = res)
      .catch(() => console.log("Error while fetching metadata"));
  }
});
</script>

<style>
.footer {
  position: absolute;
  left: 0;
  bottom: 0;
  height: 40px;
  line-height: 40px;
  width: 100%;
  text-align: center;
  background: #24292e;
  color: eee;
  border-top: solid 1px #eee;
}
#metadata {
  color: 333;
}
.modal-body-metadata {
  line-height: 24px;
  text-align: left;
}
.metadata {
  font: monospace;
  font-size: .8em;
  line-height: 1.2em;
  border-top: solid 1px #ece9e0;
  border-bottom: solid 1px #ece9e0;
  padding: 6px;
  margin: 0 0 24px 0;
  background: #fcf9f3;
  color: #555;
}
</style>
