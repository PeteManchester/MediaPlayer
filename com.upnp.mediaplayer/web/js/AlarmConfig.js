$.ajaxSetup({
	// Disable caching of AJAX responses */
	cache : false
});

var initialValue = "";

$(document).on(
		'pageshow',
		'#alarmsetup',
		function() {
			
			checkStatus();
			$('#alarmupdate').prop('disabled', true);
			
			$("#textarea").keyup(function() {
				var value = $("#textarea").val();
				checkUpdateButton(value);
			})

			$("#alarmupdate").click(
					function() {
						var value = $("#textarea").val();
						// alert(value);
						$.ajax({
							type : 'POST',
							url : '/myapp/alarmconfig/update',
							contentType : "application/json; charset=utf-8",
							dataType : 'text',
							data : {
								'' : value
							},
							success : function(msg) {
								initialValue = value;
								checkUpdateButton(value);
								message('Update Alarm Result: ' + msg);
							},
							error : function(jqXHR, textStatus, errorThrown) {
								message('Error Update Alarm: ' + textStatus
										+ ' ' + errorThrown);
							}
						});
					});

		});

function checkUpdateButton(value)
{
	if (value != initialValue) {
		$('#alarmupdate').prop('disabled', false);
	} else {
		$('#alarmupdate').prop('disabled', true);
	}
}

/**
 * Call restful web service
 * 
 */
function checkStatus() {
	$.ajax({

		dataType : 'text',
		headers : {
			Accept : "application/json",
			"Access-Control-Allow-Origin" : "*"
		},
		type : 'GET',
		url : '/myapp/alarmconfig/getAlarms',
		success : function(data) {
			// alert('get here ' + data);
			$("#textarea").val(data);
			$("#textarea").val(decode(data));
			$("#textarea").prop('readonly', false);
			initialValue = $("#textarea").val();
			// Nudge the textarea to resize..
			$('#textarea').trigger('keyup');
		},
		error : function(result, errorThrown) {
			message('Error CheckStatus: ' + errorThrown)
		}

	});
}

function decode(encoded) {
	return decodeURIComponent(encoded.replace(/\+/g, " "));
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
