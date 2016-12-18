//chrome.tabs.executeScript(null,{file: "run2.js"});
var domain;
console.log("We did it again");

chrome.browserAction.onClicked.addListener(function(tab){
	console.log("Here we are");
	chrome.tabs.executeScript({
		code: 'document.body.style.backgroundColor="red"'
	});
});

chrome.extension.onMessage.addListener(
    function(request, sender, sendResponse){
        if(request.msg == "startFunc") call();
    }
);









// chrome.tabs.onClicked.addListener(function(tab){
// 	chrome.tabs.executeScript({
// 		code: 'document.body.style.backgroundColor="red"'
// 	});
// });


// function load(){
// 	console.log("I kinda work");
// 	document.getElementByTagName("div").innerHTML = "This Sucks";
// 	document.getElementById("content").innerHTML+= '<h2>First Time User</h2>';
// 	    	// <p>Please enter the ID generated from' +
// 	    	// 'the PassTap mobile app. Reload page after submission.<p><form onsubmit=\"return save()\">' +
// 	    	// '<input id=\"ID\" type=\"text\"><input type=\"submit\" value=\"Submit\"></form>';
// 	if (typeof(Storage) !== "undefined") {
// 	    // Code for localStorage/sessionStorage
// 	    var available = false;
// 	    var id = "";
// 	    try{
// 	    	key = localStorage.getItem("id");
// 	    	available = true;
// 	    }catch(err){
// 	    	available = false;
// 	    }
// 	    if(!available){
// 	    	document.getElementById("content").innerHTML= "<h2>First Time User</h2> <p>Please enter the ID generated from" +
// 	    	"the PassTap mobile app. Reload page after submission.<p><form onsubmit=\"return save()\">" +
// 	    	"<input id=\"ID\" type=\"text\"><input type=\"submit\" value=\"Submit\"></form>";
	    	
// 	    }else{
	    	
// 	    }
// 	} else {
// 	    console.log("I dont localstore")

// 	}
// }

// document.load = load;

// function save(){
// 	var text = document.getElementById("ID").value;
// 	localStorage.setItem("ID", text);
// }

// function add(){
// 	document.getElementById("content").innerHTML = '<h2>First Time User</h2>';
// }

// chrome.tabs.onUpdated.addListener(
//   function ( tabId, changeInfo, tab )
//   { 
//     if ( changeInfo.status === "complete" )
//     {
//       chrome.tabs.executeScript({
//       code: "console.log('dsff');"
//     });
//   }
// });