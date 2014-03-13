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

      $("div").trigger('create');
      $("input[type='radio']").checkboxradio().checkboxradio("refresh");
      
      
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
    // http://stackoverflow.com/questions/8922343/dynamically-creating-vertically-grouped-radio-buttons-with-jquery
	$.ajax({
		
    	dataType: 'json',
        headers: {
            Accept:"application/json",
            "Access-Control-Allow-Origin": "*"
        },
        type:'GET',
        url:'/myapp/rest/config',
        success: function(data)
        {
        	
        	$("#friendlyName").val(data.friendly_name);
        	
        	$("#select-choice-player").val(data.player);
    		$("#select-choice-player").selectmenu("refresh");
    		
    		$("#playlistMax").val(data.playlist_max);
        	
        	$("#logFileName").val(data.log_file);
        	
        	$("#savePlayList").val(data.save_local_playlist);
        	$("#savePlayList").slider("refresh");
        	
        	$("#AVTransport").val(data.enableAVTransport);
        	$("#AVTransport").slider("refresh");
        	
        	$("#songcastReceiver").val(data.enableReceiver);
        	$("#songcastReceiver").slider("refresh");
        	
        	$("#mplayerPlayList").val(data.log_file);
        	
        	$("#mplayerPath").val(data.mplayer_path);
        	
        	$("#mplayerCache").val(data.mplayer_cache);
        	
        	$("#mplayerCacheMin").val(data.mplayer_cache_min);
        	
        	$("#mpdHost").val(data.mpd_host);
        	
        	$("#mpdPort").val(data.mpd_port);
        	
        },
        error: function (result) {
        	alert("Error " + result);
        }
        
    });
	
	
}

