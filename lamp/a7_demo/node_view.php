<?php
	//require_once()
	header("Content-Type: text/html; charset=UTF-8");
	
	//echo print_r($_SERVER);
	//echo "跳转页面来至：".$_SERVER['HTTP_REFERER'];
	/*if(!isset($_SERVER['HTTP_REFERER']) || ($_SERVER['HTTP_REFERER'] != "http://192.168.15.12/a7_demo/login.html" && 
			$_SERVER['HTTP_REFERER'] != "http://192.168.15.12/a7_demo/register.html")) {
		die("禁止直接访问");
	}*/
	//echo "跳转页面来至：".$_SERVER['HTTP_REFERER'];
	session_start();
	if(empty($_SESSION["login"])) {
		header("Location: login.html");
		exit();
	}
	
	$my_sqli = new mysqli("localhost", "root", "DD0fc2c31196", "test");
	if($my_sqli->connect_error) {
		die("连接失败:".$my_sqli->connect_error);
	}
	echo "node_view_ok</br>";
	$sql = "select * from node;";
	$res = $my_sqli->query($sql);
	while($row = $res->fetch_assoc ()){
		foreach($row as $key=>$value) {
			echo "$key=$value;";
		}
		echo "</br>";
	}
	$res->free();

error_out:	
	$my_sqli->close();
?>