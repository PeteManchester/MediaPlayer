//Only one js script for all pages in my NavBar!!
//Could be a messy..
//http://stackoverflow.com/questions/15800121/why-i-have-to-put-all-the-script-to-index-html-in-jquery-mobile/15806954#15806954

$.ajaxSetup({
	// Disable caching of AJAX responses */
	cache : false
});



$(document).on('pageshow', '#jquerycomet', function() {
	//alert("StatusPage Ready");
	//clearInterval(interval);
	//alert('pageshow');
	$.ajax({
		type : 'Post',
		url  : '/grizzly-comet-counter/long_polling',
		async : true,
		cache : false,
		
		success : function(data) {
					var json = eval('(' + data + ')');
					//alert(json.counter);
					$("#title").val(json.counter);
		},
		error : function(XMLHttpRequest, textstatus, error) { 
					alert(error);
		}		
	});
	comet();
	//interval = setInterval(checkStatus, 1000);
});

function comet() {
	$.ajax({
		type : 'Get',
		url  : '/grizzly-comet-counter/long_polling',
		async : true,
		cache : false,
		
		success : function(data) {
					//alert(data);
					var json = eval('(' + data + ')');
					//alert(json.counter);
					$("#title").val(json.counter);
					setTimeout('comet()', 1000);
		},
		error : function(XMLHttpRequest, textstatus, error) { 
					alert(error);
					setTimeout('comet()', 15000);
		}		
	});
}














