    <template id="view-1">
      <app-frame>
        <h1 class="title-view-1">This is the first view</h1>
        <h2>Anyone can see it</h2>
      </app-frame>
    </template>
    <script>
      Vue.component("view-1", {
        template: "#view-1"
      });
    </script>
    <style>
      .title-view-1 {
        color: blue;
      }
    </style>
