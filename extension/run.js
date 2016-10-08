function load(){
	console.log("I kinda work");
	chrome.storage.local.get('id', function(profileObj) {
		var id = profileObj.id;
		console.log(id);
	  	if (typeof id === "undefined") {
    // No profile in storage
    	console.log("I kinda work");
    	document.getElementById("content").innerHTML= "<h3>First Time User?</h3> <p>Please enter the ID generated from" +
    	" the PassTap mobile app.<p> " +
    	"<input style=\"max-width:100\%\" id=\"ID\" type=\"text\">";
		

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
	  			url: "https://passtap.com/server.php?v1=verify&v2=" + text + "&v3=", 
	  			success: function(result){
	  			console.log("Verify: " + result);
        		if(result==1){
        			document.getElementById("content").innerHTML="<p>Success</p>";
					document.getElementById("submit").innerHTML="";
					chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
        				chrome.tabs.update(tabs[0].id, {url: tabs[0].url});
    				});
        		}else{
        			chrome.storage.local.remove('id');
        			document.getElementById("content").innerHTML="<p>Retry<p>";
					reload();
        		}
        	}});
	});
	
	
    
    // location.reload();
	return false;
}

function reload(){
	document.getElementById("content").innerHTML= "<p>Please enter the ID generated from" +
    	" the PassTap mobile app. Initial entry was incorrect.<p> " +
    	"<input id=\"ID\" type=\"text\">";
}



load();
document.getElementById("button").addEventListener('click', save);
