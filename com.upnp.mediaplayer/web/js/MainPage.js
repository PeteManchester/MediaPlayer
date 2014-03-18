//Only one js script for all pages in my NavBar!!
//Could be a messy..
//http://stackoverflow.com/questions/15800121/why-i-have-to-put-all-the-script-to-index-html-in-jquery-mobile/15806954#15806954
var interval;

$.ajaxSetup ({
    // Disable caching of AJAX responses */
    cache: false
});

$(document)
		.on(
				'pageshow',
				'#main',
				function() {
					clearInterval(interval);
					// alert("MainPage Ready");
					var response = '{ "friendly_name": "Bedroom","player":"mpd", "log_file_name": "mediaplayer.log",  "songcast_soundcard_name": "Audio [plughw:0,0]"}';

					try {
						checkStatusConfig();
					} catch (e) {
						alert(e);
					}

					$("#Stop").click(function() {
						stopMediaPlayer();
						alert("After Stop");
					});

					$("#Restart").click(function() {
						restartMediaPlayer();
					});

					$("#Reboot").click(function() {
						rebootOS();
					});

					$("#Shutdown").click(function() {
						shutdownOS();
					});
					
					//CancelButton
					$("#mpCancel").click(function() {
						checkStatusConfig();
					});
					
					//Submit Button
					$("#mpSubmit").click(function() {
						alert('Submit');
					});
					
					
				});

$(document).on('pageshow', '#status', function() {
	// alert("StatusPage Ready");
	clearInterval(interval);
	checkStatus();

	interval = setInterval(checkStatus, 1000);	

	$("#Stops").click(function() {
		stopMediaPlayer();
	});

	$("#Restarts").click(function() {
		restartMediaPlayer();
	});

	$("#Reboots").click(function() {
		rebootOS();
	});

	$("#Shutdowns").click(function() {
		shutdownOS();
	});

});

$(document).on('pageshow', '#alarm', function() {
	
	clearInterval(interval);
	setInterval(checkSleepStatus, 1000);	
	$("#alSleepSet").click(function() {
		$.ajax({

			dataType : 'text',
			headers : {
				Accept : "text/plain",
				"Access-Control-Allow-Origin" : "*"
			},
			type : 'GET',
			url : '/myapp/config/setSleepTimer',
			success : function(data) {
				//alert(data);				
			},
			error : function(result) {
				alert("Error " + result.message);
			}

		});
	});
	
	$("#alSleepCancel").click(function() {
		$.ajax({

			dataType : 'text',
			headers : {
				Accept : "text/html",
				"Access-Control-Allow-Origin" : "*"
			},
			type : 'GET',
			url : '/myapp/config/cancelSleepTimer',
			success : function(data) {
				//alert(data);				
			},
			error : function(result) {
				alert("Error " + result.message);
			}

		});
	});

	// alert("AlarmPage Ready");
	clearInterval(interval);
	$("#Stopa").click(function() {
		stopMediaPlayer();
	});

	$("#Restarta").click(function() {
		restartMediaPlayer();
	});

	$("#Reboota").click(function() {
		rebootOS();
	});

	$("#Shutdowna").click(function() {
		shutdownOS();
	});

});

/*
 * Call a Restful Web Service
 */

/**
 * Call restful web service to get the MediaPlayer Config details
 */
function checkStatusConfig() {
	// commonStatus();
	// alert('MainPage CheckStatus');
	// http://stackoverflow.com/questions/8922343/dynamically-creating-vertically-grouped-radio-buttons-with-jquery
	$
			.ajax({

				dataType : 'text',
				headers : {
					Accept : "text/html; charset=utf-8",
					"Access-Control-Allow-Origin" : "*"
				},
				type : 'GET',
				url : '/myapp/config/getConfig',
				success : function(text) {

					var data = JSON.parse(text);
					$("#friendlyName").val(
							decodeURIComponent(data.friendly_name));

					$("#select-choice-player").val(
							decodeURIComponent(data.player));
					$("#select-choice-player").selectmenu("refresh");

					$("#playlistMax")
							.val(decodeURIComponent(data.playlist_max));

					$("#logFileName").val(decodeURIComponent(data.log_file));

					$("#savePlayList").val(
							decodeURIComponent(data.save_local_playlist));
					$("#savePlayList").slider("refresh");

					$("#AVTransport").val(
							decodeURIComponent(data.enableAVTransport));
					$("#AVTransport").slider("refresh");

					$("#songcastReceiver").val(
							decodeURIComponent(data.enableReceiver));
					$("#songcastReceiver").slider("refresh");

					$("#mplayerPlayList")
							.val(decodeURIComponent(data.log_file));

					$("#mplayerPath")
							.val(decodeURIComponent(data.mplayer_path));

					$("#mplayerCache").val(
							decodeURIComponent(data.mplayer_cache));

					$("#mplayerCacheMin").val(
							decodeURIComponent(data.mplayer_cache_min));

					$("#mpdHost").val(decodeURIComponent(data.mpd_host));

					$("#mpdPort").val(decodeURIComponent(data.mpd_port));

				},
				error : function(result) {
					alert("Error " + result);
				}

			});
}

/**
 * Call restful web service
 * 
 */
function checkStatus() {
	$.ajax({

		dataType : 'json',
		headers : {
			Accept : "application/json",
			"Access-Control-Allow-Origin" : "*"
		},
		type : 'GET',
		url : '/myapp/config/getStatus',
		success : function(data) {
			$('#status-table tr').remove();
			$('#status-table > tbody:last').append(
					'<tr> <th>Java Version</th> <td class="title">'
							+ data.java_version + '</a></td> </tr>');
			$('#status-table > tbody:last').append(
					'<tr> <th>MediaPlayer Version</th> <td class="title">'
							+ data.version + '</a></td> </tr>');
			$('#status-table > tbody:last').append(
					'<tr> <th>Start Time</th> <td class="title">'
							+ data.mp_starttime + '</a></td> </tr>');
			$('#status-table > tbody:last').append(
					'<tr> <th>Current Time</th> <td class="title">'
							+ data.mp_currenttime + '</a></td> </tr>');
			$('#status-table > tbody:last').append(
					'<tr> <th>Memory Heap Used</th> <td class="title">'
							+ data.memory_heap_used + '</a></td> </tr>');
			$('#status-table > tbody:last').append(
					'<tr> <th>Memory NonHeap Used</th> <td class="title">'
							+ data.memory_nonheap_used + '</a></td> </tr>');
			$('#status-table > tbody:last').append(
					'<tr> <th>CPU Time</th> <td class="title">' + data.cpu_time
							+ '</a></td> </tr>');
			
			
		},
		error : function(result) {
			alert("Error " + result);
		}

	});
}


/**
 * Call restful web service
 * 
 */
function checkSleepStatus() {
	$.ajax({

		dataType : 'text',
		headers : {
			Accept : "text/plain",
			"Access-Control-Allow-Origin" : "*"
		},
		type : 'GET',
		url : '/myapp/config/getSleepTimer',
		success : function(data) {
			//alert(data);
			$("#alSleepStatus").val(data);
		},
		error : function(result) {
			alert("Error " + result);
		}

	});
}
