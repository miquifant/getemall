    <template id="unauthorized">
      <app-frame>
        <h1 class="title-unauthorized">I'm sorry, {{ $javalin.state.currentUser }}</h1>
        <h2>As <em>{{ $javalin.state.currentRole }}</em>, you don't have permission to see this page</h2>
      </app-frame>
    </template>
    <script>
      Vue.component("unauthorized", {
        template: "#unauthorized"
      });
    </script>
    <style>
      .title-unauthorized {
        color: #f50;
      }
    </style>
