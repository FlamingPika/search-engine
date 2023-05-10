<%@ page import="java.util.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="engine.SearchEngine" %>



<!DOCTYPE HTML>
<!--
	Editorial by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html>

<head>
  <title>Home Page</title>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
        integrity="sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2" crossorigin="anonymous">
  <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@100;400;500;700;900&display=swap"
        rel="stylesheet">
  <link rel="stylesheet" href="assets/css/main.css" />
  <link rel="stylesheet" href="assets/css/styles.css" />
</head>

<body class="is-preload">

<!-- Header -->
<nav class="navbar navbar-expand-md navbar-dark bg-dark cfg" style="
      font-family: 'Montserrat';
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 18px;
      letter-spacing: 0.3em;
      ">
  <a class="navbar-brand" href="form.html" style="
          font-weight: 500;
          letter-spacing: 0.1em;
          color: #FFFFFF;
          text-align: center;
      "><img class="logo" src="./assets/images/LOGO_UST_white.png" width="30" height="45"/></a>
  <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav">
    <span class="navbar-toggler-icon"></span>
  </button>
  <div class="collapse navbar-collapse" id="navbarNav">
    <ul class="navbar-nav mr-auto">
      <li class="nav-item active">
        <a class="nav-link" href="#">COMP4321 Search Engine Project <span class="sr-only">(current)</span></a>
      </li>
    </ul>
  </div>
</nav>

<!-- Wrapper -->
<div id="wrapper">

  <!-- Main -->
  <div id="main">
    <div class="inner">

      <!-- Header -->
      <!-- <header id="header">
          <a href="index.html" class="logo"><strong>Dashboard</strong> ReAct</a>
          <ul class="icons">
              <li><a href="#" class="icon brands fa-twitter"><span class="label">Twitter</span></a></li>
              <li><a href="#" class="icon brands fa-facebook-f"><span class="label">Facebook</span></a></li>
              <li><a href="#" class="icon brands fa-snapchat-ghost"><span class="label">Snapchat</span></a>
              </li>
              <li><a href="#" class="icon brands fa-instagram"><span class="label">Instagram</span></a></li>
              <li><a href="#" class="icon brands fa-medium-m"><span class="label">Medium</span></a></li>
          </ul>
      </header> -->

      <!-- Banner -->
      <section id="container-fluid">
        <div style="text-align: center;">
          <img class="cse" src="./assets/images/cse.png" alt="" style="width:50%; height: 50%" />
        </div>
        <div class="form-group p-5">
          <input id="search-bar" type="text" class="form-control" placeholder="Search">
        </div>
        <form action="example1.jsp" method="post" id="the-form">
            <input id="actual-search" type="text" style="display: none;" name="inputValue">
        </form>

      </section>

      <!-- Section -->
      <section>
        <header class="major">
          <h2>Result</h2>
        </header>
        <div class="posts">
        <%

            SearchEngine se = new SearchEngine(300, "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
            List<HashMap<String, String>> l;
            if(request.getParameter("inputValue")!=null && request.getParameter("inputValue")!="")
            {
                String s = request.getParameter("inputValue");
                l = se.search(s);
                HashMap<String, String> h;
                for (int i = 0; i < l.size(); ++i) {
                    h = l.get(i);
                    out.write("<article>");
                    out.write("<strong>");
                    out.println(h.get("title"));
                    out.write("</strong>");
                    out.write("<ul>");
                    out.write("<li>");
                    out.println(h.get("url"));
                    out.write("</li>");
                    out.write("<li>");
                    out.println(h.get("info"));
                    out.write("</li>");
                    out.write("<li>");
                    out.println(h.get("freq"));
                    out.write("</li>");
                    out.write("<li>");
                    out.println(h.get("parent"));
                    out.write("</li>");
                    out.write("<li>");
                    out.println(h.get("child"));
                    out.write("</li>");
                    out.write("</ul>");
                    out.write("</article>");
                }
            }

        %>
          <article>

          </article>

        </div>
      </section>

    </div>
  </div>

  <div id="sidebar" style="display: none;">
      <div class="inner">

        <!-- Section -->
        <section>
          <img class="profile" src="./assets/images/me.png" />
          <header class="major">
            <h2>Wilson Thiesman</h2>
          </header>
          <p>Hi, I am the guy who worked on this simple yet challenging (for me personally) search engine. If you found any bugs, feel free to reach me out at my email below.. or spot me on campus (will graduate at 2024)</p>
          <ul class="contact">
            <li class="icon solid fa-envelope"><a href="#">wthiesman@connect.ust.hk</a></li>
            <li class="icon solid fa-home">The Hong Kong University of Science and Technology<br />
              Clear Water Bay, Hong Kong</li>
          </ul>
        </section>

      </div>
    </div>


</div>

<!-- Scripts -->
<script src="js/jquery.min.js"></script>
<script src="js/browser.min.js"></script>
<script src="js/breakpoints.min.js"></script>
<script src="js/util.js"></script>
<script src="js/main.js"></script>
<script src="js/script.js"></script>


<script src='https://kit.fontawesome.com/a076d05399.js'></script>
<script src="https://code.jquery.com/jquery-3.5.1.slim.min.js" integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js" integrity="sha384-9/reFTGAW83EW2RDu2S0VKaIzap3H66lZH81PoYlFhbGU+6BZp6G7niu735Sk7lN" crossorigin="anonymous"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js" integrity="sha384-B4gt1jrGC7Jh4AgTPSdUtOBvfO8shuf57BaghqFfPlYxofvL8/KUEfYiJOMMV+rV" crossorigin="anonymous"></script>


</body>

</html>