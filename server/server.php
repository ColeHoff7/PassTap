<?php

if(isset($_REQUEST['v1']) && isset($_REQUEST['v2']) && isset($_REQUEST['v3'])){
	
	$cmd = $_REQUEST['v1'];
	$id = $_REQUEST['v2'];
	$var = $_REQUEST['v3'];

	$servername = "localhost";
	$username = "tonetwgv_passtap";
	$password = "ZQqGA8[]}T@#";
	$database = "tonetwgv_passtap";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $database);

	// Check connection
	if ($conn->connect_error) {
	    die("DB Connection failed: " . $conn->connect_error);
	}


	/* +--------------------------------------------------+
	   |                                                  |
	   |     Begin List of Available Server Functions     |
	   |                                                  |
	   +--------------------------------------------------+
	*/



	// Checks if an access token is a valid access token for an account
	if($cmd == "verify"){

		$sql = "SELECT * FROM access_tokens WHERE token = '".$id."'";
		$result = mysqli_query($conn, $sql);

		if (mysqli_num_rows($result) > 0) {
			echo 1;
		}else{
			echo 0;
		}
	}

	// checks if an account (via an access token) has a password entry for a given domain name yet
	if($cmd == "checkDomain"){

		$var = str_replace("www.","",$var);

		$sql = "SELECT * FROM access_tokens at INNER JOIN accounts a ON at.account_id = a.id INNER JOIN domains d ON d.account_id = a.id WHERE at.token = '".$id."' AND d.domain = '".$var."'";
		$result = mysqli_query($conn, $sql);

		if (mysqli_num_rows($result) > 0) {
			echo 1;
		}else{
			echo 0;
		}

	}

	// sends a request to the mobile app for a private key. Once a private key is obtained, it will store the generated password in the database until the client gets it, at which point it will be deleted
	if($cmd == "generatePass"){

		$phone_id 	= "something";

		$message 	= "Hey mate, you gotta get me dat private key doe";
		$title 		= "Private Key Please";
		$subtitle	= "Give me some of that private key pls";
		$ticker 	= "What the hell is a ticker???";
		
		sendAndroidMessage($phone_id, $message, $title, $subtitle, $ticker);


	}

	// sends a request to the mobile app for a private key. Once a private key is obtained, it will store the generated password in the database until the client gets it, at which point it will be deleted
	if($cmd == "getPass"){

		$phone_id 	= "something";

		$message 	= "Hey mate, you gotta get me dat private key doe";
		$title 		= "Private Key Please";
		$subtitle	= "Give me some of that private key pls";
		$ticker 	= "What the hell is a ticker???";
		
		sendAndroidMessage($phone_id, $message, $title, $subtitle, $ticker);


	}

	if($cmd == "generateKey"){

		$key = generateRandomString();
		$keyHash = crypt($key, SHA-256); 


		$sql = "INSERT INTO accounts (phone_id, salt, private_hash) VALUES ('".$id."', '".$salt."', '".$keyHash."')";
		$result = mysqli_query($conn, $sql);

		$accessToken = generateRandomString(10);
		$last_id = mysqli_insert_id($conn);

		$sql = "INSERT INTO access_tokens (account_id, token) VALUES ('".$last_id."', '".$accessToken."')";
		$result = mysqli_query($conn, $sql);

		echo '{ "private_key": "'.$key.'", "access_token":"'.$accessToken.'"}';

	}

	if($cmd == "updateToken"){

		$sql = "SELECT * FROM accounts WHERE private_hash"


	}

	if($cmd == "checkPass"){

		$var = str_replace("www.","",$var);

		$sql = "SELECT * FROM access_tokens at INNER JOIN accounts a ON at.account_id = a.id INNER JOIN domains d ON d.account_id = a.id WHERE at.token = '".$id."' AND d.domain = '".$var."'";
		$result = mysqli_query($conn, $sql);

		if (mysqli_num_rows($result) > 0) {

			$row = mysqli_fetch_assoc($result);
			if(!$row['pass'] || $row['pass']== null || $row['pass'] == ""){
				echo 0;
			}else{
				echo $row['pass'];
				$sql = "UPDATE access_tokens at INNER JOIN accounts a ON at.account_id = a.id INNER JOIN domains d ON d.account_id = a.id SET d.pass = '' WHERE at.token = '".$id."' AND d.domain = '".$var."'";
		$result = mysqli_query($conn, $sql);
			}
			
		}else{
			echo 0;
		}
	}



}else{
	
	echo "INVALID REQUEST, NOT ENOUGH PARAMETERS GIVEN";
}




function generateRandomString($length = 100) {
    $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $charactersLength = strlen($characters);
    $randomString = '';
    for ($i = 0; $i < $length; $i++) {
        $randomString .= $characters[rand(0, $charactersLength - 1)];
    }
    return $randomString;
}




function sendAndroidMessage($phone_id, $message, $title, $subtitle, $ticker){
	// API access key from Google API's Console
	define( 'API_ACCESS_KEY', 'AIzaSyCVLkbfdq3rYbEYq_oIq0xDWfQdBXy4C-4' );
	$registrationIds = array( $phone_id );
	// prep the bundle
	$msg = array
	(
		'message' 	=> $message,
		'title'		=> $title,
		'subtitle'	=> $subtitle,
		'tickerText'	=> $ticker,
		'vibrate'	=> 1,
		'sound'		=> 1,
		'largeIcon'	=> 'large_icon',
		'smallIcon'	=> 'small_icon'
	);
	$fields = array
	(
		'registration_ids' 	=> $registrationIds,
		'data'			=> $msg
	);
	 
	$headers = array
	(
		'Authorization: key=' . API_ACCESS_KEY,
		'Content-Type: application/json'
	);
	 
	$ch = curl_init();
	curl_setopt( $ch,CURLOPT_URL, 'https://android.googleapis.com/gcm/send' );
	curl_setopt( $ch,CURLOPT_POST, true );
	curl_setopt( $ch,CURLOPT_HTTPHEADER, $headers );
	curl_setopt( $ch,CURLOPT_RETURNTRANSFER, true );
	curl_setopt( $ch,CURLOPT_SSL_VERIFYPEER, false );
	curl_setopt( $ch,CURLOPT_POSTFIELDS, json_encode( $fields ) );
	$result = curl_exec($ch );
	curl_close( $ch );
	echo $result;
}


?> 