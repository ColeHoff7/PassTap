var id;
var user;

console.log("We made it");

if($("[type=password]").length){
	chrome.storage.local.get('id', function(profileObj) {
		id = profileObj.id;
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
	        				chrome.storage.local.set({'user': user}, function(value){
	        					console.log("Saved username");
	        				});
	       	 				generatePass();
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
  		$.ajax({
  			url: "https://passtap.com/server.php?v1=getPass&v2=" + id + "&v3=" + document.domain, 
  			success: function(result){
  				timer = setInterval(check, 2000);
    	}});
    }

    function generatePass(){
    	$.ajax({
  			url: "https://passtap.com/server.php?v1=generatePass&v2=" + id + "&v3=" + document.domain, 
  			success: function(result){
  				timer = setInterval(check, 2000);
    	}});
    }

    function check(){
    	$.ajax({
  			url: "https://passtap.com/server.php?v1=checkPass&v2=" + id + "&v3=" + document.domain, 
  			success: function(result){
  				console.log("Return: " + result);
  				if(result == 0){
  					return;
  				}
  				last = $("input").first();
  				$("input").each(function(){
  					if($(this).attr("type") == "password"){
  						last.val("User Name Here");
  						clearInterval(timer);
  					}
  					last = $(this);
  				});
  				$("[type=password]").val(result);
  			}});
    }