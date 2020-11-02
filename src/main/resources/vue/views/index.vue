    <template id="index">
      <app-frame>
        <h1 class="title-index">Hello, World!</h1>
        <center>
          <img src="/images/logo_100b.png" alt="Getemall">
        </center>
      </app-frame>
    </template>
    <script>
      Vue.component("index", {
        template: "#index"
      });
    </script>
    <style>
      .title-index {
        color: goldenrod;
      }
    </style>
