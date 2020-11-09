    <template id="header">
      <header>
        <nav>
          <a href="/index"><img id="logo" src="/images/logo_100w.png" alt="Getemall"></a>
          <span id="loggedInUser" v-if="$javalin.state.currentUser">{{ $javalin.state.currentUser }}</span>
          <ul id="menu">
            <li><a href="/v1" title="Public content">View 1</a></li>
            <li><a href="/v2" title="Logged in users only">View 2</a></li>
            <li><a href="/v3" title="Admins only">View 3</a></li>
            <li><a href="/v4" title="You will not find it">View 4</a></li>
            <li v-if="$javalin.state.currentUser"><form method="post" action="/logout"><button id="logout">Log out</button></form></li>
            <li v-if="! $javalin.state.currentUser"><a href="/login">Log in</a></li>
          </ul>
        </nav>
      </header>
    </template>
    <script>
      Vue.component("page-header", {
        template: "#header"
      });
    </script>
    <style>
      form {
        margin: 0px;
        padding: 0px;
      }
      #logo {
        max-height: 50px;
      }
      header {
        background: #16425f;
        border-bottom: 5px solid #b48a00;
        box-shadow: 0 1px 0 0 rgba(0, 0, 0, .6);
      }
      #loggedInUser {
        border: solid 1px #b48a00;
        background-color: #b48a00;
        color: white;
        border-radius: 0 0 6px 6px;
        box-shadow: 2px 2px 2px 0 rgba(0, 0, 0, .4);
        padding: 0 15 3;
        font-weight:bold;
        font-size: .8em;
        top: 0px;
        right: 0px;
        position: absolute;
      }
      nav {
        padding: 15px;
        margin: 0 auto;
        max-width: 800px;
        position: relative;
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        color: #b6e2ff;
      }
      nav #menu {
        float: right;
      }
      a {
        text-decoration: none;
        color: #008ab4;
      }
      #menu li {
        float: left;
        margin: 0 10px;
      }
      #menu li a , #logout {
        background: transparent;
        cursor: pointer;
        border: 0;
        font-size: 16px;
        display: inline-block;
        color: white;
        text-align: center;
        height: 30px;
        line-height: 30px;
        padding: 0 10px;
        text-decoration: none;
      }
      nav ul, nav li {
        margin: 0;
        padding: 0;
        list-style-type: none;
      }

      /* Needlessly fancy menu hover effect */
      #menu li {
        position: relative;
      }
      #menu li a::after, #logout:after {
        position: absolute;
        top: 28px;
        left: 0;
        width: 100%;
        height: 4px;
        background: rgba(255, 255, 255, 0.5);
        border-radius: 5px;
        content: '';
        opacity: 0;
        -webkit-transition: opacity 0.3s, -webkit-transform 0.3s;
        transition: opacity 0.3s, transform 0.3s;
        -webkit-transform: translateY(10px);
        transform: translateY(10px);
      }
      #logout:hover::after,
      #logout:focus::after,
      #menu li a:hover::after,
      #menu li a:focus::after {
          opacity: 1;
          -webkit-transform: translateY(0px);
          transform: translateY(0px);
      }
    </style>
