var id;
var user;
var domain = document.domain;
console.log("We made it");

if($("[type=password]").length){
	chrome.storage.local.get('id', function(profileObj) {
		id = profileObj.id;
    console.log(id);
	  	if (typeof id === "undefined") {
	  		console.log("Uninitialized user")
	  	}else{
	  		$.ajax({
	  			url: "https://passtap.com/server.php?v1=checkDomain&v2=" + id + "&v3=" + document.domain, 
	  			success: function(result){
	  				console.log("checkDomain: "+ result);
	        		if(result==1){
	        			//has used domain
                checkFill();
	        		}else if(result==0){
	        			//hasn't used domain
	        			if (confirm('Would you like to generate a password for this site?')) {
	        				user = prompt("Username/Email:");
	       	 				generatePass();
                  chrome.storage.local.set({domain: -1});
	    				   }
	        		}else{
	        			//invalid id
	        			chrome.storage.local.remove('id');
	        			location.reload();
	        		}
        		}
        	});
	  		
	  	}	

});
}

	var timer;

	function checkFill(){
   chrome.storage.local.get(domain, function(autofill){
      console.log("autonum: " + autofill[domain]);
      fill = autofill[domain];
      if(fill==1){
        $.ajax({
          url: "https://passtap.com/server.php?v1=getPass&v2=" + id + "&v3=" + document.domain, 
          success: function(result){
            console.log(result);
            timer = setInterval(check, 2000);
        }});
      } 
      if(fill == 0){
        if(confirm("Would you like to get your password? \n Change this setting in your extension.")){
          $.ajax({
            url: "https://passtap.com/server.php?v1=getPass&v2=" + id + "&v3=" + document.domain, 
            success: function(result){
              console.log(result);
              timer = setInterval(check, 2000);
          }});
        }
      }
    });
  }

    function generatePass(){
    	$.ajax({
  			url: "https://passtap.com/server.php?v1=generatePass&v2=" + id + "&v3=" + "{\"username\":\""+ user +"\",\"domain\":\""+ document.domain +"\"}", 
  			success: function(result){
  				console.log("GeneratePass: " + result);
  				timer = setInterval(check, 2000);
    	}});
    }

    function check(){
    	$.ajax({
  			url: "https://passtap.com/server.php?v1=checkPass&v2=" + id + "&v3=" + document.domain, 
  			success: function(result){
  				var data = JSON.parse(result);
  				console.log("Return: " + result);
  				if(result == 0){
  					return;
  				}
  				last = $("input").first();
  				$("input").each(function(){
  					if($(this).attr("type") == "password"){
  						last.val(data["user"]);
  						clearInterval(timer);
  					}
  					last = $(this);
  				});
  				$("[type=password]").val(data["password"]);
  			}});
    }



    function onRequest(request, sender, sendResponse) {
      console.log("got message");  
      if(request.msg == 'fill'){
        $.ajax({
            url: "https://passtap.com/server.php?v1=getPass&v2=" + id + "&v3=" + document.domain, 
            success: function(result){
              console.log(result);
              timer = setInterval(check, 2000);
          }});
      }
      if(request.msg == 'reset'){
        // $.ajax({
        //         url: "https://passtap.com/server.php?v1=resetPass&v2=" + id + "&v3=" + domain, 
        //         success: function(result){
        //             console.log("Reset: " + result);
        //             if(result==1){
        //                $("#submit").attr("hidden","true");
        //                 chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
        //                     chrome.tabs.update(tabs[0].id, {url: tabs[0].url});
        //                 });
        //             }

        // if($('input[id*="current"]').length != 0) {
        //   $('input[id*="current"]').val("old");
        //   $('input[id*="current"]').next().val("new");
        //   $('input[id*="current"]').next().next().val("new");
        // }else if($('input[id*="old"]').length != 0){
        //   $('input[id*="old"]').val("old");
        //   $('input[id*="old"]').next().val("new");
        //   $('input[id*="old"]').next().next().val("new");
        // }else{
          $('input[type=password]').val("old");
          $('input[type=password]').next().val("new");
          $('input[type=password]').next().next().val("new");

        //   }
        // });

        // }  
      }
    }



    chrome.extension.onMessage.addListener(onRequest);
