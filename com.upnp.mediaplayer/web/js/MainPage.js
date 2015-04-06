//Only one js script for all pages in my NavBar!!
//Could be a messy..
//http://stackoverflow.com/questions/15800121/why-i-have-to-put-all-the-script-to-index-html-in-jquery-mobile/15806954#15806954
var interval;
var config_json = null;

$.ajaxSetup({
	// Disable caching of AJAX responses */
	cache : false
});

$(document)
		.on(
				'pageshow',
				'#main',
				function() {
					clearInterval(interval);
					var response = '{ "friendly_name": "Bedroom","player":"mpd", "log_file_name": "mediaplayer.log",  "songcast_soundcard_name": "Audio [plughw:0,0]"}';

					try {
						checkStatusConfig();
					} catch (e) {
						message('Error CheckConfig: ' + e.ErrorMessage);
					}

					$("#Stop").click(function() {
						stopMediaPlayer();
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

					// CancelButton
					$("#mpCancel").click(function() {
						checkStatusConfig();
					});

					// Submit Button
					$("#mpSubmit").click(function() {
						updateConfig();
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
		var value = $("#slider-time").val();
		$.ajax({
			dataType : 'text',
			headers : {
				Accept : "text/plain",
				"Access-Control-Allow-Origin" : "*"
			},
			type : 'GET',
			url : '/myapp/alarm/setSleepTimer?value=' + value,
			success : function(data) {
				message('Set Sleep: ' + data);
			},
			error : function(result, errorThrown) {
				message('Error Set Sleep: ' + errorThrown);
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
			url : '/myapp/alarm/cancelSleepTimer',
			success : function(data) {
				message('Sleep Cancel: ' + data);
			},
			error : function(result, errorThrown) {
				message('Error SleepCancel: ' + errorThrown);
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
					config_json = data;
					$("#friendlyName").val(
							decode(data.mediaplayer_friendly_name));

					$("#select-choice-player").val(
							decode(data.mediaplayer_player));
					$("#select-choice-player").selectmenu("refresh");

					$("#playlistMax").val(decode(data.mediaplayer_playlist_max));

					$("#logFileName").val(decode(data.log_file_name));

					$("#savePlayList").val(
							decode(data.mediaplayer_save_local_playlist));
					$("#savePlayList").slider("refresh");

					$("#AVTransport").val(
							decode(data.mediaplayer_enable_avTransport));
					$("#AVTransport").slider("refresh");

					$("#slider-volume").val(data.mediaplayer_startup_volume);
					$("#slider-volume").slider("refresh");

					$("#slider-volume_max").val(data.mediaplayer_max_volume);
					$("#slider-volume_max").slider("refresh");

					$("#songcastReceiver").val(decode(data.mediaplayer_enable_receiver));
					$("#songcastReceiver").slider("refresh");

					$("#mplayerPlayList").val(decode(data.mplayer_playlist));

					$("#mplayerPath").val(decode(data.mplayer_path));

					$("#mplayerCache").val(decode(data.mplayer_cache_size));

					$("#mplayerCacheMin").val(decode(data.mplayer_cache_min));

					$("#mpdHost").val(decode(data.mpd_host));

					$("#mpdPort").val(decode(data.mpd_port));

					$("#log_file_level").val(decode(data.log_file_level));
					$("#log_file_level").selectmenu("refresh");

					$("#log_console_level").val(decode(data.log_console_level));
					$("#log_console_level").selectmenu("refresh");

					$("#log_openhome_level").val(
							decode(data.openhome_log_level));
					$("#log_openhome_level").selectmenu("refresh");

					$("#openhome_port").val(decode(data.openhome_port));

					$("#java_soundcard_suffix").val(
							decode(data.java_soundcard_suffix));
					
					$("#java_sound_software_mixer_enabled").val(
							decode(data.java_sound_software_mixer_enabled));
					$("#java_sound_software_mixer_enabled").slider("refresh");

					$("#songcast_latency").val(
							decode(data.songcast_latency_enabled));
					$("#songcast_latency").slider("refresh");

					$("#http_web_port").val(decode(data.web_server_port));
					
					$("#web_server_enabled").val(
							decode(data.web_server_enabled));
					$("#web_server_enabled").slider("refresh");

					//$("#tunein_username").val(
					//		decode(data.radio_tunein_username));
					
					$("#airplay_enabled").val(
							decode(data.airplay_enabled));
					$("#airplay_enabled").slider("refresh");
					
					$("#airplay_latency_enabled").val(
							decode(data.airplay_latency_enabled));
					$("#airplay_latency_enabled").slider("refresh");
					
					$("#airplay_port").val(decode(data.airplay_port));
					
					$("#airplay_audio_start_delay").val(
							decode(data.airplay_audio_start_delay));
					$("#airplay_audio_start_delay").slider("refresh");
					
					
				},
				error : function(result,errorThrown) {
					message('Error CheckSleepConfig: ' + errorThrown);
				}

			});
}

function updateConfig() {
	// alert('Here');
	if (config_json != null) {

		config_json.mediaplayer_friendly_name = $("#friendlyName").val();

		config_json.mediaplayer_player = $("#select-choice-player").val();

		config_json.mediaplayer_playlist_max = $("#playlistMax").val();

		config_json.log_file_name = $("#logFileName").val();

		config_json.mediaplayer_save_local_playlist = $("#savePlayList").val();

		config_json.mediaplayer_enable_avTransport = $("#AVTransport").val();

		config_json.mediaplayer_startup_volume = $("#slider-volume").val();
		
		config_json.mediaplayer_max_volume = $("#slider-volume_max").val();

		config_json.mediaplayer_enable_receiver = $("#songcastReceiver").val();

		config_json.mplayer_playlist = $("#mplayerPlayList").val();

		config_json.mplayer_path = $("#mplayerPath").val();

		config_json.mplayer_cache_size = $("#mplayerCache").val();

		config_json.mplayer_cache_min = $("#mplayerCacheMin").val();

		config_json.mpd_host = $("#mpdHost").val();

		config_json.mpd_port = $("#mpdPort").val();

		config_json.log_file_level = $("#log_file_level").val();

		config_json.log_console_level = $("#log_console_level").val();

		config_json.openhome_log_level = $("#log_openhome_level").val();

		config_json.openhome_port = $("#openhome_port").val();

		config_json.java_soundcard_suffix = $("#java_soundcard_suffix").val();
		
		config_json.java_sound_software_mixer_enabled = $("#java_sound_software_mixer_enabled").val();

		config_json.songcast_latency_enabled = $("#songcast_latency").val();

		config_json.web_server_port = $("#http_web_port").val();

		config_json.radio_tunein_username = $("#tunein_username").val();
		
		config_json.web_server_enabled = $("#web_server_enabled").val();
		
		config_json.airplay_enabled = $("#airplay_enabled").val();
		
		config_json.airplay_latency_enabled = $("#airplay_latency_enabled").val();
		
		config_json.airplay_port = $("#airplay_port").val();
		
		config_json.airplay_audio_start_delay = $("#airplay_audio_start_delay").val();
		
		
		$.ajax({
			type : 'POST',
			url : '/myapp/config/setConfig',
			contentType : "application/json; charset=utf-8",
			dataType : 'text',
			data : {
				'' : JSON.stringify(config_json)
			},
			success : function(msg) {
				message('Update Config Result: ' + msg);
			},
			error : function(jqXHR, textStatus, errorThrown) {
				message('Error UpdateConfig: ' + textStatus + ' ' + errorThrown);
			}
		});

	}
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
		url : '/myapp/status/getStatus',
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
			$("#textarea").val(decode(data.log_events));
			$("#textarea").prop('readonly', true);
			//Nudge the textarea to resize..
			$('#textarea').trigger('keyup');
			//$('textarea').css({'height': 'auto'});

		},
		error : function(result, errorThrown) {
			message('Error CheckStatus: ' +  errorThrown)
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
		url : '/myapp/alarm/getSleepTimer',
		success : function(data) {
			// alert(data);
			$("#alSleepStatus").val(data);
		},
		error : function(result, errorThrown) {
			message('Error CheckSleepStatus: ' + errorThrown);
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




