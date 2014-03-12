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
    setInterval(checkStatus,1000);

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
$.ajax({
		
    	dataType: 'json',
        headers: {
            Accept:"application/json",
            "Access-Control-Allow-Origin": "*"
        },
        type:'GET',
        url:'/myapp/rest/status',
        success: function(data)
        {
        	$('#status-table tr').remove();
        	$('#status-table > tbody:last').append('<tr> <th>Java Version</th> <td class="title">' + data.java_version  +'</a></td> </tr>');
        	$('#status-table > tbody:last').append('<tr> <th>MediaPlayer Version</th> <td class="title">' + data.version  +'</a></td> </tr>');
        	$('#status-table > tbody:last').append('<tr> <th>Memory Heap Used</th> <td class="title">' + data.memory_heap_used  +'</a></td> </tr>');
        	$('#status-table > tbody:last').append('<tr> <th>Memory NonHeap Used</th> <td class="title">' + data.memory_nonheap_used  +'</a></td> </tr>');
        	$('#status-table > tbody:last').append('<tr> <th>CPU Time</th> <td class="title">' + data.cpu_time  +'</a></td> </tr>');
        },
        error: function (result) {
        	//alert("Error " + result);
        }
        
    });

}

