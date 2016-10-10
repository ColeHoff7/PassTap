<?php

if(isset($_REQUEST['v1']) && isset($_REQUEST['v2']) && isset($_REQUEST['v3'])){
	
	$cmd = $_REQUEST['v1'];
	$id = $_REQUEST['v2'];
	$var = $_REQUEST['v3'];
	$var = str_replace("www.","",$var);

	$servername = "localhost";
	$username = "XXXXXXXX";
	$password = "XXXXXXXX";
	$database = "XXXXXXXX";

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

		$sql = "SELECT * FROM access_tokens at INNER JOIN accounts a ON at.account_id = a.id INNER JOIN domains d ON d.account_id = a.id WHERE at.token = '".$id."' AND d.domain = '".$var."'";
		$result = mysqli_query($conn, $sql);

		if (mysqli_num_rows($result) > 0) {
			echo 1;
		}else{
			echo 0;
		}

	}

	// Makes a new domain entry, then authorizes a password for it
	if($cmd == "generatePass"){

		$var = json_decode($var, true);

		$sql = "SELECT * FROM access_tokens at INNER JOIN accounts a ON at.account_id = a.id WHERE at.token = '".$id."'";
		$result = mysqli_query($conn, $sql);

		if (mysqli_num_rows($result) == 1) {

			$cmd = "getPass"; // Also run get pass after this

			$row = mysqli_fetch_assoc($result);
			$account_id = $row['account_id'];

			$salt = generateRandomString(10);

			$sql = "INSERT INTO domains (account_id, domain, username, salt) VALUES ('".$account_id."', '".$var["domain"]."','".$var["username"]."','".$salt."')";
			$result = mysqli_query($conn, $sql);

			$var = $var["domain"];


		} elseif (mysqli_num_rows($result) > 2) {

			// reached in error, domain entry already exists. Go to reset pass for this domain
			$cmd = "resetPass";

		} else{
			echo "ACCESS KEY NO LONGER VALID";
		}
		

	}


	// Modifies an existing domain entry, and then requests a password for it
	if($cmd == "resetPass"){

		$sql = "SELECT * FROM access_tokens at INNER JOIN accounts a ON at.account_id = a.id WHERE at.token = '".$id."'";
		$result = mysqli_query($conn, $sql);

		if (mysqli_num_rows($result) > 0) {

			$row = mysqli_fetch_assoc($result);
			$account_id = $row['account_id'];

			$sql = "DELETE FROM domains WHERE account_id = '".$account_id."' AND domain = '".$var."'";
			$result = mysqli_query($conn, $sql);

			if($result){
				echo 1;
			}else{
				echo 0;
			}


		}else{
			echo "ACCESS KEY NO LONGER VALID OR DOMAIN ENTRY DOESN'T EXIST";
		}
		

	}

	// sends a request to the mobile app for a private key. Once a private key is obtained, it will store the generated password in the database until the client gets it, at which point it will be deleted
	if($cmd == "getPass"){

		$sql = "SELECT * FROM access_tokens at INNER JOIN accounts a ON a.id = at.account_id INNER JOIN domains d ON d.account_id = a.id WHERE at.token = '".$id."' AND d.domain = '".$var."'";
		$result = mysqli_query($conn, $sql);

		$phone_id = "NO_PHONE_FOUND";

		if (mysqli_num_rows($result) > 0) {

			$row = mysqli_fetch_assoc($result);

			$phone_id = $row['phone_id'];

		}else{

			die("INVALID ACCESS CODE OR DOMAIN DOESN'T EXIST YET");
		}

		$message 	= $var;
		$title 		= "PassTap Auth Request";
		$subtitle	= "Click here to authorize your request";
		$ticker 	= "";
		
		sendAndroidMessage($phone_id, $message, $title, $subtitle, $ticker);


	}


	if($cmd == "getAllPasswords"){


		// TODO, this would be useful on the app

		$sql = "SELECT * FROM access_tokens at INNER JOIN accounts a ON a.id = at.account_id INNER JOIN domains d ON d.account_id = a.id WHERE at.token = '".$id."'";

		echo 0;

	}


	// this salt is only used for verifying a private key since we don't have a lookup table for constant time salt lookup for private keys off the phone.
	// The real passwords are salted with unique salt values on the database
	$auth_salt = "uPIsomOpNedsadsaiKdwasddJfPaiHh";


	if($cmd == "setPass"){

		$keyHash = crypt($id, $auth_salt); 

		$sql = "SELECT * FROM accounts a INNER JOIN domains d on a.id = d.account_id WHERE a.private_hash = '".$keyHash."' AND d.domain = '".$var."'";
		$result = mysqli_query($conn, $sql);

		$row = mysqli_fetch_assoc($result);

		$account_id = $row['account_id'];
		$domain = $row['domain'];
		$salt = $row['salt'];

		$pass = crypt($var.$id.$salt, $salt);

		$sql = "UPDATE accounts a INNER JOIN domains d on d.account_id = a.id SET d.pass = '".$pass."' WHERE a.id = '".$account_id."' AND d.domain = '".$var."'";
		$result = mysqli_query($conn, $sql);

		if($result){
			echo 1;
		}else{
			echo 0;
		}

	}

	if($cmd == "generateKey"){

		$key = generateRandomString();
		$keyHash = crypt($key, $auth_salt); 


		$sql = "INSERT INTO accounts (phone_id, private_hash) VALUES ('".$id."', '".$keyHash."')";
		$result = mysqli_query($conn, $sql);

		$accessToken = generateRandomString(10);
		$last_id = mysqli_insert_id($conn);

		$sql = "INSERT INTO access_tokens (account_id, token) VALUES ('".$last_id."', '".$accessToken."')";
		$result = mysqli_query($conn, $sql);

		echo '{ "private_key": "'.$key.'", "access_token":"'.$accessToken.'"}';

	}

	if($cmd == "updateToken"){

		$sql = "SELECT * FROM accounts";
		$result = mysqli_query($conn, $sql);

		if (mysqli_num_rows($result) > 0) {
		    while($row = mysqli_fetch_assoc($result)) {

		    	if(crypt($id, $auth_salt) == $row['private_hash']){

		    		$sql = "UPDATE accounts SET phone_id = '".$var."' WHERE id = ".$row['id'];
					$result = mysqli_query($conn, $sql);

					echo 1;

					return;
		    	}

		    }

		    echo 0;
		}else{
			echo "ERROR, NO ACCOUNT ASSOCIATED WITH THAT PASSWORD";
		}


	}

	if($cmd == "checkPass"){


		$sql = "SELECT * FROM access_tokens at INNER JOIN accounts a ON at.account_id = a.id INNER JOIN domains d ON d.account_id = a.id WHERE at.token = '".$id."' AND d.domain = '".$var."'";
		$result = mysqli_query($conn, $sql);

		if (mysqli_num_rows($result) > 0) {

			$row = mysqli_fetch_assoc($result);
			if(!$row['pass'] || $row['pass']== null || $row['pass'] == ""){
				echo 0;
			}else{
				echo '{"user":"'.$row['username'].'","password":"'.$row['pass'].'"}';
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
	// echo "Phone Id: ".$phone_id;
	$registrationIds = array( $phone_id );
	// prep the bundle
	// $msg = array
	// (
	// 	'message' 	=> $message,
	// 	'title'		=> $title,
	// 	'subtitle'	=> $subtitle,
	// 	'tickerText'	=> $ticker,
	// 	'vibrate'	=> 1,
	// 	'sound'		=> 1,
	// 	'largeIcon'	=> 'large_icon',
	// 	'smallIcon'	=> 'small_icon'
	// );
	$notification = array(
		'message' 	=> $message,
		'title'		=> $title,
		'text'		=> $subtitle,
		//'tickerText'	=> $ticker,
		'vibrate'	=> 1,
		'sound'		=> 1,
		'largeIcon'	=> 'icon',
		'smallIcon'	=> 'icon',
		'click_action' => 'AUTHENTICATE'
	);
	$fields = array
	(
		'registration_ids' 	=> $registrationIds,
		'notification' 		=> $notification,
		'data'			=> array('domain' => $message)
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