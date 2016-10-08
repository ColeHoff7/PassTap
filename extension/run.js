window.onload = function load(){

	if (typeof(Storage) !== "undefined") {
	    // Code for localStorage/sessionStorage.
	    var available = false;
	    var id = "";
	    try{
	    	key = localStorage.getItem("id");
	    	available = true;
	    }catch{
	    	available = false;
	    }
	    if(!available){
	    	document.getElementById("content").innerHTML= 
	    	"<h2>First Time User</h2> <p>Please enter the ID generated from" +
	    	"the PassTap mobile app. Reload page after submission.<p><form onsubmit=\"return save()\">" +
	    	"<input id=\"ID\" type=\"text\"><input type=\"submit\" value=\"Submit\"></form>"
	    }else if{
	    	
	    }else{
	    	
	    }
	} else {
	    // Sorry! No Web Storage support..

	}
}

function save(){
	var text = document.getElementById("ID").value;
	localStorage.setItem("ID", text);
}