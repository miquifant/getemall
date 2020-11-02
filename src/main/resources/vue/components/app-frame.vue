    <template id="app-frame">
      <div class="app-frame">
        <slot></slot>
      </div>
    </template>
    <script>
      Vue.component("app-frame", {
        template: "#app-frame"
      });
    </script>
    <style>
      html {
        font-family: sans-serif;
      }
      .app-frame {
        max-width: 800px;
        margin: 0 auto;
        padding: 15px 20px;
        background: #fff;
        box-shadow: 0 1px 5px rgba(0, 0, 0, 0.25);
      }
    </style>
