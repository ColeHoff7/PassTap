console.log("We made it");

if($("[type=password]").length){
	chrome.storage.local.get('id', function(profileObj) {
		var id = profileObj.id;
		console.log(id);
	  	if (typeof id === "undefined") {
	  		console.log("Uninitialized user")
	  	}else{
	  		$.ajax({
	  			url: "https://passtap.com/server.php?v1=checkDomain&v2=" + id + "&v3=" + document.domain, 
	  			success: function(result){
        		if(result==1){
        			//has used domain
        			checkFill();
        		}else if(result==0){
        			//hasn't used domain
        			if (confirm('Would you like to generate a password for this site?')) {
       	 				generatePass();
    				}
        		}else{
        			//invalid id

        		}
        	}
        });
	  		
	  	}	

});
}


	function checkFill(){
  		$.ajax({
  			url: "https://passtap.com/server.php?v1=getPass&v2=" + id + "&v3=" + document.domain, 
  			success: function(result){
    		$("[type=password]").val(result);
    	}});
    }

    function generatePass(){
    	$.ajax({
  			url: "https://passtap.com/server.php?v1=generatePass&v2=" + id + "&v3=" + document.domain, 
  			success: function(result){
    		$("[type=password]").val(result);
    	}});
    }