//Only one js script for all pages in my NavBar!!
//Could be a messy..
//http://stackoverflow.com/questions/15800121/why-i-have-to-put-all-the-script-to-index-html-in-jquery-mobile/15806954#15806954

$.ajaxSetup({
	// Disable caching of AJAX responses */
	cache : false
});



$(document).on('pageshow', '#jquerycomet', function() {
	$("#textarea").prop('readonly', true);
	$("#title").prop('readonly', true);
	$.ajax({
		type : 'Post',
		dataType : 'text',
		headers : {
			Accept : "text/html; charset=utf-8",
			"Access-Control-Allow-Origin" : "*"
		},
		url  : '/grizzly-comet-counter/long_polling',
		async : true,
		cache : false,
		
		success : function(text) {
					var data = $.parseJSON(text);
					$("#title").val(decode(data.details));
					$("#textarea").val(decode(data.lyrics));
					$('#textarea').trigger('keyup');
		},
		error : function(result, errorThrown) {
			message(errorThrown)
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
		url  : '/grizzly-comet-counter/long_polling',
		async : true,
		cache : false,
		
		success : function(text) {
					var data = $.parseJSON(text);
					$("#title").val(decode(data.details));
					$("#textarea").val(decode(data.lyrics));
					$('#textarea').trigger('keyup');
					setTimeout('comet()', 1000);
		},
		error : function(result, errorThrown) {
					message(errorThrown)
					setTimeout('comet()', 15000);
		}		
	});
}

function decode(encoded) {
	return decodeURIComponent(encoded.replace(/\+/g, " "));
}

function message(text){
	$("<div class='ui-loader ui-overlay-shadow ui-body-e ui-corner-all'><p>" + text + "</p></div>").css({ "display": "block", "opacity": 0.96, "top": $(window).scrollTop() + 100 })
	.appendTo( $.mobile.pageContainer )
	.delay( 1500 )
	.fadeOut( 400, function(){
	  $(this).remove();
	});
	}














