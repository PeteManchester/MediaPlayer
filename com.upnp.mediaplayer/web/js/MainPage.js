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
	commonStatus();
	//alert('MainPage CheckStatus');
    // http://stackoverflow.com/questions/8922343/dynamically-creating-vertically-grouped-radio-buttons-with-jquery
	$.ajax({
		
    	dataType: 'text',
        headers: {
            Accept:"text/html; charset=utf-8",
            "Access-Control-Allow-Origin": "*"
        },
        type:'GET',
        url:'/myapp/config/getConfig',
        success: function(text)
        {
        	
        	var data = JSON.parse(text);
        	$("#friendlyName").val(decodeURIComponent(data.friendly_name));
        	
        	$("#select-choice-player").val(decodeURIComponent(data.player));
    		$("#select-choice-player").selectmenu("refresh");
    		
    		$("#playlistMax").val(decodeURIComponent(data.playlist_max));
        	
        	$("#logFileName").val(decodeURIComponent(data.log_file));
        	
        	$("#savePlayList").val(decodeURIComponent(data.save_local_playlist));
        	$("#savePlayList").slider("refresh");
        	
        	$("#AVTransport").val(decodeURIComponent(data.enableAVTransport));
        	$("#AVTransport").slider("refresh");
        	
        	$("#songcastReceiver").val(decodeURIComponent(data.enableReceiver));
        	$("#songcastReceiver").slider("refresh");
        	
        	$("#mplayerPlayList").val(decodeURIComponent(data.log_file));
        	
        	$("#mplayerPath").val(decodeURIComponent(data.mplayer_path));
        	
        	$("#mplayerCache").val(decodeURIComponent(data.mplayer_cache));
        	
        	$("#mplayerCacheMin").val(decodeURIComponent(data.mplayer_cache_min));
        	
        	$("#mpdHost").val(decodeURIComponent(data.mpd_host));
        	
        	$("#mpdPort").val(decodeURIComponent(data.mpd_port));
        	
        },
        error: function (result) {
        	alert("Error " + result);
        }
        
    });
	
	
}


