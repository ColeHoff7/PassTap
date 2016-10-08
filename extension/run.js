function load(){
	console.log("I kinda work");
	chrome.storage.local.get('id', function(profileObj) {
		var id = profileObj.id;
		console.log(id);
	  	if (typeof id === "undefined") {
    // No profile in storage
    	console.log("I kinda work");
    	document.getElementById("content").innerHTML= "<h2>First Time User</h2> <p>Please enter the ID generated from" +
    	"the PassTap mobile app. Reload page after submission.<p> " +
    	"<input id=\"ID\" type=\"text\">";
		

  	} else {
    // Profile exists in storage
    	$("#submit").attr("hidden","true");
    	console.log(chrome.storage.local.get("id"), function(result){
    		console.log(id.result);
    	});
    	

  	}
	});
}

// document.load = load;

function save(){
	console.log('starting');
	var text = document.getElementById("ID").value;
	chrome.storage.local.set({'id': text}, function(txt){
		console.log('ID saved' + text);
		$.ajax({
	  			url: "https://passtap.com/server.php?v1=verify&v2=" + id + "&v3=", 
	  			success: function(result){
        		if(result==1){
        			document.getElementById("content").innerHTML="<p>Success</p>";
					document.getElementById("submit").innerHTML="";
        		}else{
        			chrome.storage.local.remove({'id': text});
        			document.getElementById("content").innerHTML="<p>Retry<p>";
					document.getElementById("submit").innerHTML="";
        		}
        	}});
	});
	location.reload();
	return false;
}


load();
document.getElementById("button").addEventListener('click', save);
