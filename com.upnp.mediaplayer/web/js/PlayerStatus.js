//Only one js script for all pages in my NavBar!!
//Could be a messy..
//http://stackoverflow.com/questions/15800121/why-i-have-to-put-all-the-script-to-index-html-in-jquery-mobile/15806954#15806954
var interval;
var config_json = null;

$.ajaxSetup({
	// Disable caching of AJAX responses */
	cache : false
});



$(document).on('pageshow', '#status', function() {
	// alert("StatusPage Ready");
	clearInterval(interval);
	checkStatus();
	interval = setInterval(checkStatus, 1000);
});





/**
 * Call restful web service
 * 
 */
function checkStatus() {
	//alert('GetPlayerStatus');
	$.ajax({

		dataType : 'json',
		headers : {
			Accept : "application/json",
			"Access-Control-Allow-Origin" : "*"
		},
		type : 'GET',
		url : '/myapp/player/getStatus',
		success : function(data) {
			//alert(data);
			$("#title").val(decode(data.title));
			$("#album_title").val(decode(data.album_title));
			$("#album_artist").val(decode(data.album_artist));
			$("#time_played").val(secondstotime(decode(data.time_played)));
			$("#image_uri").attr("src",data.image_uri);
			
			
		},
		error : function(result, errorThrown) {
			message('Error CheckStatus: ' +  errorThrown)
		}

	});
}


function decode(encoded) {
	return decodeURIComponent(encoded.replace(/\+/g, " "));
}

function secondstotime(secs)
{
    var t = new Date(1970,0,1);
    t.setSeconds(secs);
    var s = t.toTimeString().substr(0,8);
    if(secs > 86399)
    	s = Math.floor((t - Date.parse("1/1/70")) / 3600000) + s.substr(2);
    //alert(s);
    return s;
//	var time = parseInt(secs,10)
//	var minutes = Math.floor(time / 60);
//	var seconds = time % 60;
//
//	if(minutes<10)
//	{
//	    minutes = '0' + minutes;
//	}
//
//	if(seconds<10)
//	{
//	    seconds = '0' + seconds;
//	}
//
//	return minutes+":"+seconds;
}


function message(text){
$("<div class='ui-loader ui-overlay-shadow ui-body-e ui-corner-all'><p>" + text + "</p></div>").css({ "display": "block", "opacity": 0.96, "top": $(window).scrollTop() + 100 })
.appendTo( $.mobile.pageContainer )
.delay( 1500 )
.fadeOut( 400, function(){
  $(this).remove();
});
}




