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

function playTweets(){
  $iframe[0].contentWindow.playTweets();
}

function resetTweets(){
  $iframe[0].contentWindow.resetTweets();
}

function displayAllTweets(){
  $("#playTweets").removeClass("active").html("Play");
  $iframe[0].contentWindow.displayAllTweets();
}

function clearTweets(){
  $iframe[0].contentWindow.clearTweets();
  resetTweets();
}

function hideAll(){
  clearTweets();
  $("#resetTweets").removeClass("hide").html("Show All");
}

function stopPlaying(){ 
  resetTweets();
  $("#playTweets").removeClass("active").html("Play");
}

function changeTypes(){
  stopPlaying();
  hideAll();
}

function useTweets(){
  changeTypes();
  $iframe[0].contentWindow.useTweets();
  clearTweets();
  showTweetLabels();
}

function useSentiment(){ 
  changeTypes();
  $iframe[0].contentWindow.useSentiment();
  clearTweets();
  showSentimentLabels();
}

function changeLabels(contents){
  $(".sideBar .labels").hide("fast", function(){
    $(this).html(contents).show("fast");
  });
}

function showTweetLabels(){
  changeLabels("<p class=\"Tweet\">Tweet</p><p class=\"retweet\">Retweet</p>");
}

function showSentimentLabels(){
  changeLabels("<p class=\"pos\">Positive</p><p class=\"neg\">Negative</p>");
}

$(document).ready(function(){
  $iframe = $("#usmap");
  $("#usmap").load(function(){
    $(".fadeMessageBox .status").addClass("complete").html("Complete!");
    setTimeout(function() { removeFade(); }, 500);
    $("#playTweets").click();
    showTweetLabels();
  });

  $(".optionList a.option").click(function(){
    if(!$(this).hasClass("selected")){
      id = $(this).attr("id");
      $(".optionList a.option.selected").removeClass("selected");
      $(this).addClass("selected");
      if(id == "useTweets") useTweets();
      else if(id == "useSentiment") useSentiment();
    }
    return false;
  });

  $("#playTweets").click(function(){
    if($(this).hasClass("active")){
      stopPlaying()
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
      hideAll();
    } else {
      displayAllTweets();
      $(this).addClass("hide").html("Hide All");
    }
    return false;
   });
});
