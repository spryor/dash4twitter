function removeFadeBox() {
  $(".fadeMessageBox .container").animate({left: -1500}, "normal", function(){
    $(".fadeMessageBox").fadeOut("normal", function(){
      $(this).remove();
      $(".fadeBox").fadeOut("normal", function(){
        $(this).remove();
      });
    });
  });
}

function showFadeBox(stat, details) {
  contents = "<span class=\"status\">"+stat+"</span>"
  contents += "<span class=\"details\">"+details+"</span>"
  if($(".fadeMessageBox").length < 1) {
    fadebox = "<div class=\"fadeBox\"></div>"
    fadebox += "<div class=\"fadeMessageBox\">"
    fadebox += "<div class=\"container\">"+contents+"</div>"
    $(fadebox).hide().appendTo("body").fadeIn("fast");
  } else {
    $(".fadeMessageBox .container").html(contents);
  }
}

function removeInitialFadeBox() {
  $(".fadeMessageBox .status").html("Complete!");
  setTimeout(removeFadeBox, 800);
}

function initStatus(newStatus) {
  $(".fadeMessageBox .details").html(newStatus);
}

function showSocketLoader() {
  $("#socketLoader").show("fast");
}

function hideSocketLoader() {
  $("#socketLoader").hide("fast");
}

/*
Handle Web Socket
*/
var tweetStreamer;
var dataRefresher;
var wsUri = "ws://localhost:9000"; 
function addMessage(text) {
  set = $("#output p")
  if(set.length >= 4) set.first().slideUp("fast", function(){$(this).remove();})
  $("<p>"+text+"</p>").hide().appendTo("#output").slideDown("fast"); 
}

function initWebSocket() { initStatus("Opening web socket."); openWebSocket(); } 

function openWebSocket() {
  websocket = new WebSocket(wsUri);
  websocket.onopen = function(evt) { onOpen(evt) };
  websocket.onclose = function(evt) { onClose(evt) };
  websocket.onmessage = function(evt) { onMessage(evt) };
  websocket.onerror = function(evt) { onError() }; 
} 

function onOpen(evt) { initStatus("Web socket open."); removeInitialFadeBox(); streamTweetFromBuffer(); }

function onClose(evt) { 
  websocket.close(); 
  window.clearInterval(tweetStreamer);
  window.clearInterval(dataRefresher);
  showFadeBox("Oops, the connection was lost.", "Try refreshing the page to restart the connection."); 
} 

function onMessage(evt) {
  var json = jQuery.parseJSON( evt.data )
  if(json.type == "keywordUpdate") {
    addNewKeywords(json.data);
    addNewTermSentiment(json.sentiment);
    addNewFilters(json.filters);
  } else if(json.type == "keywordRefresh") {
    addNewKeywords(json.data);
    addNewTermSentiment(json.sentiment);
  } else if(json.type == "tweet") {
    addTweet(json.data);
  }
  hideSocketLoader();
}  

function onError(evt) { addMessage('<span style="color: red;">ERROR:</span> ' + evt.data); } 

function doSend(act, msg) {
   websocket.send(JSON.stringify({action: act, message: msg }));
}

function streamTweetFromBuffer(){doSend("stream", "");}

function grabKeywords(keywords){showSocketLoader(); doSend("getkeywords", keywords);}

function refreshData(){doSend("refreshData", "");}

function removeFilter(filterName){showSocketLoader(); doSend("removeFilter", filterName); dataRefresher(); }

function addTweet(data) {
  tweetLoader = $("#tweetContainer .resultBox .results .tweetStreamerLoader");
  if(tweetLoader.length > 0) tweetLoader.remove();
  set = $("#tweetContainer .resultBox .results a")
  if(set.length >= 30) set.last().slideUp("fast", function(){$(this).remove();})
  var label;
  if(data.label == "pos") label = "positive"
  else if(data.label == "neg") label = "negative"
  else label = "neutral"
  var newTweet = $("<a href=\"https://twitter.com/"+data.screenName+"/status/"+data.id+"\" class=\""+label+"\" target=\"_blank\"><img src=\""+data.userImage+"\" class=\"profileImage\"><div class=\"names\"><span class=\"screenName\">"+data.username+"</span> <span class=\"username\">@"+data.screenName+"</span></div>"+data.tweet+"</a>")
  newTweet.hide().prependTo("#tweetContainer .resultBox .results").slideDown("fast");
  action = $("#tweetContainer .resultBox .actions ul a.selected").attr("href");
  target = action.substring(1, action.length);
  if(action != "#all" && !newTweet.hasClass(target)) newTweet.animate({opacity:.3}, "fast")
}

function addNewFilters(filters) {
  $("#nav .filterList .filters a").remove();
  newFilters = "";
  for(var i = 0; i < filters.length; i ++) {
    classAssignment = "";
    if(filters[i][0]=='k') classAssignment = "class=\"keyword\"";
    newFilters += "<a href=\"#"+filters[i]+"\" "+classAssignment+">"+filters[i]+"</a>\n"
  }
  $("#nav .filterList .filters").prepend(newFilters);
  //prep the new filter links for clicking

  $("#nav .filterList .filters a").click(function(){
    $(this).slideUp("fast", function(){
      removeFilter($(this).attr("href").substr(1));
      $(this).remove();
      if($("#nav .filterList .filters a.keyword").length < 1) {
        hideCommands();
      }
    });
    return false;
  });
}

/* -------------- WEB SOCKET - END -------------- */

/*
 * A function to allow for simple display of the primary commands
 * (the left hand commands like keywords, trends, etc.)
 */
function showCommands() {
  if($("#nav .filterList").is(":hidden")) {
    var i = 1;
    $("#nav .commandList a, #nav .filterList").each(function(){
      var current = $(this);
      current.css({left: -20,opacity: 0, display: "block"});
      setTimeout(function(){ current.animate({left: 0,opacity: 1}, "fast"); }, i*100);
      i++;
    });
  }
}

/*
 * A function to hide the primary commands
 */
function hideCommands() {
  var i = 1;
  $("#nav .commandList a.selected").click();
  $($("#nav .commandList a, #nav .filterList").get().reverse()).each(function(){
    var current = $(this);
    setTimeout(function(){ current.animate({left: -20,opacity: 0}, "fast", function(){$(this).hide()}); }, i*100);
    i++;
  });
}

/*
 * A function to add and remove the class "selected" from 
 * a particular selection as and add actions for selecting
 * and deselecting
 */
function addSelect(loc, allowDeselect, action, exitAction) {
  initStatus("Adding select options.")
  $(loc).click(function(){
    if(!$(this).hasClass("selected")){
      $(loc+".selected").removeClass("selected");
      $(this).addClass("selected");
      action($(this));
    } else if(allowDeselect) {
      $(this).removeClass("selected");
      if(exitAction) exitAction(function(){});
    }
    return false;
  });
}

function addNewKeywords(data) {
  $(".resultBox.keywords .results").remove();
  for(keyword in data) {
    links = "";
    for(var i = 0; i < data[keyword].length; i++) {
      var keywordType;
      if(data[keyword][i][0] == "@") keywordType = "mention"
      else if(data[keyword][i][0] == "#") keywordType = "hashtag"
      else keywordType = "keyword"
      links += "<a href=\"#\" class=\""+keywordType+"\">"+data[keyword][i]+"</a>";
    }
    if(links == "") links = "Insufficient Data"
    $("<div class=\"results\"><h3>\""+keyword+"\"</h3>"+links+"</div>").appendTo(".resultBox.keywords");
  }
  //Allows keywords to be draggable
  $("#content .resultBox.keywords .results a").draggable({
    revert: false, // when not dropped, the item will revert back to its initial position
    helper: "clone",
    cursor: "move"
  });
}

function addNewTermSentiment(data) {
  $("#sentContainer .resultBox .results").remove();
  $(".tooltip").remove();
  var i = 0;
  
  for(keyword in data) {
    i++;
    var id = "sentimentChart"+i;
    var content = ""
    if(data[keyword].length < 1) content = "Insufficient Data"
    $("<div class=\"results\" id=\""+id+"\"><h3>\""+keyword+"\"</h3><span>"+content+"</span></div>").appendTo("#sentContainer .resultBox");
    if(content == "") {
      var dataset = [
        {label: "Neutral", value: data[keyword][2]}, 
        {label: "Negative", value: data[keyword][1]}, 
        {label: "Positive", value: data[keyword][0]}
      ];

      var width = 300,
      height = 300,
      radius = Math.min(width, height) / 2;

      var color = //d3.scale.category10();
        d3.scale.ordinal()
        .range(["#1f77b4", "#d62728", "#2ca02c"]);

      var arc = d3.svg.arc()
        .outerRadius(radius - 10)
        .innerRadius(radius * .4);

      var pie = d3.layout.pie()
        .sort(null)
        .value(function(d) { return d.value; });

      var svg = d3.select("#"+id+" span").append("svg")
        .attr("width", width)
        .attr("height", height)
        .append("g")
        .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

      dataset.forEach(function(d) {
        d.value = +d.value;
      });

      var g = svg.selectAll(".arc")
        .data(pie(dataset))
        .enter().append("g")
        .attr("class", "arc");

      g.append("path")
        .attr("d", arc)
        .style("fill", function(d) { return color(d.data.label); });

      g.append("text")
        .attr("transform", function(d) {
          var a = (d.startAngle + d.endAngle) * 90 / Math.PI - 90;
          var angle = a > 90 ? a - 180 : a;
          if(angle == 90) angle = 0; //if there is only one label, make it horizontal
          return "translate(" + arc.centroid(d) + ")rotate(" + angle +")"; 
        })
        //.attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")"; })
        .attr("dy", ".30em")
        .style("text-anchor", "middle")
        .text(function(d) { 
          if(d.data.value > 0) return d.data.label;
          return ""; 
        });

      g.on("mouseover", function(d, i){
        var tooltip = $('<p class="tooltip"></p>')
          .text(d.data.value)
          .appendTo('body')
          .show()
          .css({opacity: 0, background: color(d.data.label)})
          .animate({opacity: 1}, "fast");
        $(this).mousemove(function(e) {
            var mousex = e.pageX + 20; //Get X coordinates
            var mousey = e.pageY + 10; //Get Y coordinates
            tooltip.css({ top: mousey, left: mousex })
          });
      }).on("mouseout", function(d, i){
        $('.tooltip').remove();
      });
    }
  }
}

/*
 * A function to control the functionality of selecting or 
 * deselcting options in the keyword submenu
 */
function keywordAction(element) {
  action = element.attr("href");
  target = action.substring(1);
  if(action == "#all"){
    $(".resultBox.keywords .results a").animate({opacity:1}, "fast");
  } else {
    $(".resultBox.keywords .results a:not(."+target+")").animate({opacity:.3}, "fast")
    $(".resultBox.keywords .results a."+target).animate({opacity:1}, "fast");
  }
}

function tweetStreamAction(element) {
  action = element.attr("href");
  target = action.substring(1, action.length);
  if(action == "#all"){
    $("#tweetContainer .results a").animate({opacity:1}, "fast");
  } else {
    $("#tweetContainer .results a:not(."+target+")").animate({opacity:.3}, "fast")
    $("#tweetContainer .results a."+target).animate({opacity:1}, "fast");
  }
}

/*
 * A function for displaying a result box (containers for 
 * submenu data)
 */
function showResultBox(element) {
  $("#content .resultBox").stop(true, true).css({left: -1200});
  $(".resultContainer").stop(true, true).hide();
  $(element.attr("href")).stop(true, true).hide().css({overflow: "hidden",opacity:1}).show("fast", function(){
    $("#content .resultBox").animate({left: 0}, "normal", function(){
      $(element.attr("href")).css({overflow: "visible"});
    });
  });
}

/*
 * A function to hide a resultBox element
 */
function hideResultBox(callback) {
  $(".resultContainer").stop(true, true).css({overflow: "hidden",opacity:0}).hide(0, function(){
    $("#content .resultBox").css({left: -1200});
  });
}

/*
 * A function to check if the user's browser is IE
 */
function isIE(){
  return $('html').is('.ie6, .ie7, .ie8, .ie9')
}

/*
 * If the browser does not support HTML5 placeholder
 * attributes, initialize them with Javascript
 */
function initializePlaceholders(){
  if(isIE()){
    initStatus("Initializing placeholders")
    $("input[type=text]").each(function(){
      var placeholder = $(this).attr("placeholder")
      if($(this).val().trim() == "") $(this).val(placeholder);
      $(this).focus(function(){
        if($(this).val().trim() == placeholder) $(this).val("");
      });
      $(this).blur(function(){
        if($(this).val().trim() == "") $(this).val(placeholder);
      });
    });
  }
}

function initializeKeywordSearch() {
  initStatus("Initializing keyword query functionality.")
  $("#searchKeywordForm").submit(function(){ 
    var sBox = $(".search_input");
    var contents = sBox.val().trim();
    if(contents != sBox.attr("placeholder").trim() && contents != "") {
      sBox.val("");
      grabKeywords(contents);
      window.clearInterval(tweetStreamer);
      tweetStreamer = setInterval(streamTweetFromBuffer,2000);
      window.clearInterval(dataRefresher);
      dataRefresher = setInterval(refreshData,5000);
      showCommands();
    }
    return false;
  });
}

$(document).ready(function(){
  initializePlaceholders();
  addSelect("#nav .commandList a", true, showResultBox, hideResultBox);
  initializeKeywordSearch();
  initWebSocket();
  addSelect(".resultBox.keywords .actions ul a", false, keywordAction);
  addSelect("#tweetContainer .resultBox .actions ul a", false, tweetStreamAction);
   
  $("#content .resultBox").css({left: 0});

  //Allows dragged keywords to be droppable in the search bar
  $(".search_input").droppable({
    activeClass: "query-state-ha",
    hoverClass: "query-state-hover",
    drop: function( event, ui ) {
      contents = $(this).val().trim()
      if(contents == $(this).attr("placeholder").trim()) contents = "";
      dragged = ui.draggable.html().trim()
      if(contents != ""){ dragged = ", "+dragged;}
      $(this).val(contents+dragged);
    }
  });
});
