
  $("#Stop").click(function(){
	    alert('Stop');
	  });
  $("#Restart").click(function(){
	  alert('Restart');
	  });
  $("#Reboot").click(function(){
	  alert('Reboot');
	  });
  
  $("#Shutdown").click(function(){
	  alert('Shutdown');
  });
  
  function commonStatus()
  {
	  alert('CommonJS');
  }
  
  /*
   * Too much repitition, need to refactor when I get time
   */
  function stopMediaPlayer()
  {
	  var JSONObject= {
	      "command":"sudo service mediaplayer stop"
	  };
	  
	  $.ajax({  
          url:'/myapp/execute/command',  
          type:'post',
          data :  JSONObject,      
          dataType: 'JSON',
          success: function(data) { 
                   var jsonData = $.parseJSON(data); //if data is not json
              }  
      });  
  }
  
  function restartMediaPlayer()
  {
	  var JSONObject= {
	      "command":"sudo service mediaplayer restart"
	  };
	  
	  $.ajax({  
          url:'/myapp/execute/command',  
          type:'post',
          data :  JSONObject,      
          dataType: 'JSON',
          success: function(data) { 
                   var jsonData = $.parseJSON(data); //if data is not json
              }  
      });  
  }
  
  function rebootOS()
  {
	  var JSONObject= {
	      "command":"sudo reboot"
	  };
	  
	  $.ajax({  
          url:'/myapp/execute/command',  
          type:'post',
          data :  JSONObject,      
          dataType: 'JSON',
          success: function(data) { 
                   var jsonData = $.parseJSON(data); //if data is not json
              }  
      });  
  }
  
  function shutdownOS()
  {
	  var JSONObject= {
	      "command":"sudo shutdown -y -g0"
	  };
	  
	  $.ajax({  
          url:'/myapp/execute/command',  
          type:'post',
          data :  JSONObject,      
          dataType: 'JSON',
          success: function(data) { 
                   var jsonData = $.parseJSON(data); //if data is not json
              }  
      });  
  }
  
  
