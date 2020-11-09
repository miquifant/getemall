    <template id="footer">
      <footer>
        Getemall vers. <strong>{{ metadata.version }}</strong>.
      </footer>
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
            .catch(() => Console.log("Error while fetching metadata"));
        }
      });
    </script>
    <style>
      footer {
        position: absolute;
        left: 0;
        bottom: 0;
        height: 40px;
        line-height: 40px;
        width: 100%;
        text-align: center;
        background: #fff;
        border-top: 1px solid #008ab4;
        font-size: 14px;
      }
    </style>
