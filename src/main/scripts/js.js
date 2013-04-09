function removeFade() {
  $(".fadeMessageBox .container").animate({left: -1000}, "normal", function(){
    $(".fadeMessageBox").fadeOut("normal", function(){
      $(this).remove();
      $(".fadeBox").fadeOut("normal", function(){
        $(this).remove();
      });
    });
  });
}

var $iframe;

function playTweets()
{
  $iframe[0].contentWindow.playTweets();
}

function resetTweets()
{
  $iframe[0].contentWindow.resetTweets();
}

function displayAllTweets()
{
  $("#playTweets").removeClass("active").html("Play");
  $iframe[0].contentWindow.displayAllTweets();
}

function clearTweets()
{
  $iframe[0].contentWindow.clearTweets();
  resetTweets();
}

$(document).ready(function() {
  $iframe = $("#usmap");
  $("#usmap").load(function(){
    $(".fadeMessageBox .status").addClass("complete").html("Complete!");
    setTimeout(function() { removeFade(); }, 500);
    $("#playTweets").click();
  });

  $(".optionList a.option").click(function(){
    if(!$(this).hasClass("selected")){
      $(".optionList a.option.selected").removeClass("selected");
      $(this).addClass("selected");
    }
    return false;
  });

  $("#playTweets").click(function(){
    if($(this).hasClass("active")){
      resetTweets();
      $(this).removeClass("active").html("Play");
    } else {
      playTweets();
      $(this).addClass("active").html("Stop");
      if($("#resetTweets").hasClass("hide"))
        $("#resetTweets").removeClass("hide").html("Show All");
    }

    return false;
  });
 
  $("#resetTweets").click(function(){
    if($(this).hasClass("hide")) {
      clearTweets();
      $(this).removeClass("hide").html("Show All");
    } else {
      displayAllTweets();
      $(this).addClass("hide").html("Hide All");
    }
    return false;
   });
});