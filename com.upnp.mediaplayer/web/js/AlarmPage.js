$(document).ready(function(){
	var news= "Players (mpd and mplayer) Config..";
	var info= "Songcast Config....";
	var lyrics = "Media Player Config....";
	var title = "Change the Config";
	var response = '{ "friendly_name": "Bedroom","player":"mpd", "log_file_name": "mediaplayer.log",  "songcast_soundcard_name": "Audio [plughw:0,0]"}';
	
    try {	    	

		 
    }
    catch(e)
    {
  	  alert(e);
    }
  
	
	checkStatus();
    //setInterval(checkStatus,10000);

  $("#Status").click(function(){
	    //$("#text").text(lyrics);
	    //$("#header").text(title);
	  });
  $("#Config").click(function(){
	    // alert('Info');
	  //$("#text").text(info);
	  });
  $("#Track").click(function(){
	  //$("#text").text(news);
	  });
	  
	  //$('<input type="radio" name="radio-choice-1" id="radio-choice-4"><label for="radio-choice-4">Cow</label>').appendTo("fieldset");

      //$("div").trigger('create');
      //$("input[type='radio']").checkboxradio().checkboxradio("refresh");
      
      
});

/*
 * Call a Restful Web Service
 */

/**
 * Call restful web service to get the statistics Then show/hide button
 * depending on the result.
 */
function checkStatus()
{
	
}

