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

function hideCommands() {
  var i = 1;
  $("#nav .commandList a.selected").click();
  $($("#nav .commandList a, #nav .filterList").get().reverse()).each(function(){
    var current = $(this);
    setTimeout(function(){ current.animate({left: -20,opacity: 0}, "fast", function(){$(this).hide()}); }, i*100);
    i++;
  });
}

function addSelect(loc, action, exitAction) {
  $(loc).click(function(){
    if(!$(this).hasClass("selected")){
      $(loc+".selected").removeClass("selected");
      $(this).addClass("selected");
      action($(this));
    } else {
      $(this).removeClass("selected");
      if(exitAction) exitAction(function(){});
    }
    return false;
  });
}

function keywordAction(element) {
  action = element.attr("href")
  target = action.substring(1, action.length)
  target = target.substring(0, target.length - 1)
  if(action == "#all"){
    $(".resultBox.keywords .results a").animate({opacity:1}, "fast");
  } else {
    $(".resultBox.keywords .results a:not(."+target+")").animate({opacity:.3}, "fast")
    $(".resultBox.keywords .results a."+target).animate({opacity:1}, "fast");
  }
}

function showResultBox(element) {
  $("#content .resultBox").stop(true, true).css({left: -1200});
  $(".resultContainer").stop(true, true).hide().css({overflow: "hidden",opacity:1}).show("fast", function(){
    $("#content .resultBox").animate({left: 0}, "normal", function(){
      $(".resultContainer").css({overflow: "visible"});
    });
  });
}

function hideResultBox(callback) {
  $(".resultContainer").stop(true, true).css({overflow: "hidden",opacity:0}).hide(0, function(){
    $("#content .resultBox").css({left: -1200});
  });
}

function isIE(){
  return $('html').is('.ie6, .ie7, .ie8, .ie9')
}

function initializePlaceholders(){
  if(isIE()){
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

function initializeFilters(selection){
  $(selection).click(function(){
    $(this).slideUp("fast", function(){
      $(this).remove();
      if($("#nav .filterList a.keyword").length < 1) {
        hideCommands();
      }
    });
    return false;
  });
}

$(document).ready(function(){
  initializePlaceholders();
  addSelect("#nav .commandList a", showResultBox, hideResultBox);
  addSelect(".resultBox .actions ul a", keywordAction);
  initializeFilters("#nav .filterList a");
  $("#content .resultBox").animate({left: 0});
  $("#content .resultBox.keywords .results a").draggable({
    revert: false, // when not dropped, the item will revert back to its initial position
    helper: "clone",
    cursor: "move"
  });
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
  
  $("#searchKeywordForm").submit(function(){
    $("#nav .commandList a.selected").click();
    showCommands();
    var sBox = $(".search_input");
    var contents = sBox.val().trim();
    if(contents != sBox.attr("placeholder").trim() && contents != "") {
      var terms = contents.split(",")
      var newFilters = "\n";
      for(var i = 0; i < terms.length; i++)
      {
        newFilters += "<a href=\"#\" class=\"keyword\">"+terms[i]+"</a>\n"
      }
      $("#nav .filterList a.keyword").slideUp("fast", function(){$(this).remove();})
      $("#nav .filterList .filters").prepend(newFilters);
      initializeFilters("#nav .filterList a.keyword");
    }
    
    return false;
  });
});
