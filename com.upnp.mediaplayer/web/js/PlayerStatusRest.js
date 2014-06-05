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
			$('#status-table tr').remove();
			$('#status-table > tbody:last').append('<tr> <th>Artist</th> <td class="title">'+ data.album_artist + '</a></td> <th>Album</th> <td class="title">'	+ data.album_title + '</a></td> <th>Title</th> <td class="title">'	+ data.title + '</a></td> </tr>');
			$('#status-table > tbody:last').append('<tr> <th>Time</th> <td class="title">'	+ secondstotime(data.time_played) + '</a></td> <th>Time</th> <td class="title">'	+ secondstotime(data.track_duration) + '</a></td> </tr>');
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
    return s;
}


function message(text){
$("<div class='ui-loader ui-overlay-shadow ui-body-e ui-corner-all'><p>" + text + "</p></div>").css({ "display": "block", "opacity": 0.96, "top": $(window).scrollTop() + 100 })
.appendTo( $.mobile.pageContainer )
.delay( 1500 )
.fadeOut( 400, function(){
  $(this).remove();
});
}




