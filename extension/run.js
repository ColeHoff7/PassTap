var id;
var domain;
function load(){
	console.log("I kinda work");
	chrome.storage.local.get('id', function(profileObj) {
		id = profileObj.id;
		console.log(id);
        chrome.tabs.query({'active': true, 'lastFocusedWindow': true}, function (tabs) {
                var url = tabs[0].url;
                console.log(url);
                //find & remove protocol (http, ftp, etc.) and get domain
                if (url.indexOf("://") > -1) {
                    domain = url.split('/')[2];
                }
                else {
                    domain = url.split('/')[0];
                }
                //find & remove port number
                domain = domain.split(':')[0];
                console.log(domain);
        });
	  	if (typeof id === "undefined") {
    // No profile in storage
    	console.log("I kinda work");
    	document.getElementById("content").innerHTML= "<h3>First Time User?</h3> <p>Please enter the ID generated from" +
    	" the PassTap mobile app.<p> " +
    	"<input style=\"max-width:100\%\" id=\"ID\" type=\"text\">";

      	} else {
        // Profile exists in storage
        	$("#submit").attr("hidden","true");
            $("#navigation").show();

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
        			document.getElementById("content").innerHTML="<p>Press All Set on your app.</p>";
					$("#submit").attr("hidden","true");
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

function resetPass(){
        $.ajax({
                url: "https://passtap.com/server.php?v1=resetPass&v2=" + id + "&v3=" + domain, 
                success: function(result){
                    console.log("Reset: " + result);
                    if(result==1){
                    document.getElementById("content").innerHTML="<p>Password Sucesssfully Reset</p>";
                       $("#submit").attr("hidden","true");
                        chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
                            chrome.tabs.update(tabs[0].id, {url: tabs[0].url});
                        });
                    }
                }
        });
}

function settings(){
    $("#notSettings").attr("hidden","true");
    $("#navigation").show();
    $('#tabs').show();
    $('#first').removeClass("active");
    $('#sets').addClass("active");

}



function reload(){
	document.getElementById("content").innerHTML = "<p>Please enter the value generated from" +
    	" the PassTap mobile app. Previous entry was incorrect.<p> " +
    	"<input style=\"max-width:100\%\" id=\"ID\" type=\"text\">";
}

$('ul.nav-tabs li a').click(function (e) {
    $('ul.nav-tabs li.active').removeClass('in active');
    $(this).parent('li').addClass('active');
    var num = this.getAttribute("value");
    console.log(num);
    var val = 'tab' + num;
    console.log(val);
    $("div[id="+ val + "]").addClass('active');
    $('div.active').removeClass('active');
    $('#'+val).addClass('active');
});

// function first(){
//     $('#tab1').addClass('active');
// }

// function second(){
//     $('#tab2').addClass('active');
// }

// function third(){
//     $('#tab3').addClass('active');
//     console.log("here");
// }

load();
document.getElementById("button").addEventListener('click', save);
document.getElementById("res").addEventListener('click', resetPass);
document.getElementById("set").addEventListener('click', settings); 
// document.getElementById("1").addEventListener('click', first);
// document.getElementById("2").addEventListener('click', second);
// document.getElementById("3").addEventListener('click', third);

