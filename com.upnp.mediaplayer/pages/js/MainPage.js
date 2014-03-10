$(document).ready(function(){
	
	var news= "Players (mpd and mplayer) Config..";
	var info= "Songcast Config....";
	var lyrics = "Media Player Config....";
	var title = "Change the Config";
	var response = '[{ "option":"friendly_name",    "value":"Study"},    {     "option":"log_file_name",      "value":"mediaplayer.log"},    {     "option":"songcast_soundcard_name",      "value":"Audio [plughw:0,0]"}]';
	
    try {	    	
    	// convert string to JSON
    	response = $.parseJSON(response);
    	//alert("After Response");
    	$(function() {
    	    $.each(response, function(i, item) {
    	        //var $tr = $('<tr>').append( $('<td>').text(item.option), $('<td>').text(item.value)).appendTo('#my-table');
    	        //alert($tr.wrap('<p>').html());
    	    	var eachrow = "<tr>" + "<td>" + item.option + "</td>"  + "<td>" + item.value + "</td>" + "</tr>" ;
    	    	$('#tbody').append(eachrow);
    	    	//$('<tr>').html("<td>" + response[i].option + "</td><td>" + response[i].value + "</td>").appendTo('#tbody');
    	    });
    	});
		 
  }
  catch(e)
  {
  	alert(e);
  }
	
	
	checkStatus();
    // setInterval(checkStatus,1000);

  $("#MediaPlayer").click(function(){
	    $("#text").text(lyrics);
	    $("#header").text(title);
	  });
  $("#Songcast").click(function(){
	    // alert('Info');
	  $("#text").text(info);
	  });
  $("#Players").click(function(){
	  $("#text").text(news);
	  });
	  
	  $('<input type="radio" name="radio-choice-1" id="radio-choice-4"><label for="radio-choice-4">Cow</label>').appendTo("fieldset");

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
        url:'http://d067e5492684:8090/com.upnp.mediaplayer/pretech/state/Pete,Test',
        success: function(data)
        {
        	var test = data.value;
        	test = test.replace("\r\n", "<br/>");
        	$("#text").html(test);
        },
        error: function (result) {
        	alert("Error " + result);
        }
        
    });
	
}