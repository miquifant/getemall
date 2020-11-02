    <template id="view-3">
      <app-frame>
        <h1 class="title-view-3">This is the third view</h1>
        <h2>Only for admins</h2>
      </app-frame>
    </template>
    <script>
      Vue.component("view-3", {
        template: "#view-3"
      });
    </script>
    <style>
      .title-view-3 {
        color: red;
      }
    </style>
