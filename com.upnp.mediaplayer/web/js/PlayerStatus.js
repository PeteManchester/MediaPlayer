
var image_url = "";

$.ajaxSetup({
	// Disable caching of AJAX responses */
	cache : false
});

$(document).ready(function() {

});

$(document).on('pageshow', '#PlayerStatus', function() {
	
	//$width = $('#content').width();
	$width = $(window).width();
	$height= $(window).height();
	if($width > $height)
		{
			$width = $height;
		}
	if($height > $width)
	{
		$height = $width;
	}
	try
	{
	$width = ($width) - (($height/100)*18);
	}
	catch(e)
	{}
	//alert($width + ' ' + $height );
	$('#content img').css({
		          'width' : $width , 'height' : $width
     });
	

	$("#textarea").prop('readonly', true);
	$("#textarea_artist").prop('readonly', true);
	$("#title").prop('readonly', true);
	$('div[data-role="navbar"] ul li a#idlyrics').on('click', function () {
		$('#textarea').trigger('keyup');
    }); 
	$('div[data-role="navbar"] ul li a#idalbumart').on('click', function () {

    });
	
	$('div[data-role="navbar"] ul li a#idartistinfo').on('click', function () {
		$('#textarea_artist').trigger('keyup');
    });
	
	$.ajax({
		type : 'Post',
		dataType : 'text',
		headers : {
			Accept : "text/html; charset=utf-8",
			"Access-Control-Allow-Origin" : "*"
		},
		url : '/grizzly-comet/playerstatus',
		async : true,
		cache : false,

		success : function(text) {
			setDisplay(text);
		},
		error : function(result, errorThrown) {
			//alert('ErrorPageShow ' + errorThrown);
			message('ErrorPost ' + errorThrown)
		}
	});
	comet();
});

function comet() {
	$.ajax({
		type : 'Get',
		dataType : 'text',
		headers : {
			Accept : "text/html; charset=utf-8",
			"Access-Control-Allow-Origin" : "*"
		},
		url : '/grizzly-comet/playerstatus',
		async : true,
		cache : false,

		success : function(text) {
			setDisplay(text);
			comet();
		},
		error : function(result, errorThrown) {
			//alert('Comet ' + errorThrown);
			message('Error Comet ' + errorThrown)
			//setTimeout('comet()', 15000);
			comet();
		}
	});
}

function setDisplay(text) {
	try {
		var data = $.parseJSON(text);
		$("#title").val(decode(data.details));
		$('#status-table tr').remove();
		$('#status-table').css("font-weight", "normal");
		$('#status-table > tbody:last').append(
				'<tr> <th>Artist:</th> <td class="title">' + decode(data.artist)
						+ '</a></td> <th>Album:</th> <td class="title">'
						+ decode(data.album_title)
						+ '</a></td> <th>Title:</th> <td class="title">'
						+ decode(data.title) + '</a></td> </tr>');
		$('#status-table > tbody:last').append(
				'<tr> <th>Time:</th> <td class="title">'
						+ secondstotime(data.time_played)
						+ '</a></td> <th>Duration:</th> <td class="title">'
						+ secondstotime(data.track_duration)
						+ '</a></td> </tr>');
		if (image_url != data.image_uri) {
			$("#image_uri").attr("src", data.image_uri);
			$("#album_image_uri").attr("src", data.image_uri);
			image_url = data.image_uri;
		}
		$("#textarea").val(decode(data.lyrics));
		$('#textarea').trigger('keyup');
		
		$("#textarea_artist").val(decode(data.artist_biography));
		$('#textarea_artist').trigger('keyup');
	} catch (e) {
		console.log(text);
		//alert('setDisplay ' + e);
		message('Error SetDisplay: ' + e);
	}

}

function decode(encoded) {
	return decodeURIComponent(encoded.replace(/\+/g, " "));
}

function secondstotime(secs) {
	var t = new Date(1970, 0, 1);
	t.setSeconds(secs);
	var s = t.toTimeString().substr(0, 8);
	if (secs > 86399)
		s = Math.floor((t - Date.parse("1/1/70")) / 3600000) + s.substr(2);
	return s;
}

function message(text) {
	$(
			"<div class='ui-loader ui-overlay-shadow ui-body-e ui-corner-all'><p>"
					+ text + "</p></div>").css({
		"display" : "block",
		"opacity" : 0.96,
		"top" : $(window).scrollTop() + 100
	}).appendTo($.mobile.pageContainer).delay(1500).fadeOut(400, function() {
		$(this).remove();
	});
}
