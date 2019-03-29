<?php
	//require_once()
	header("Content-Type: text/html; charset=UTF-8");
	
	@$mac = $_REQUEST['mac'];
	@$longitude = $_REQUEST['longitude'];
	@$latitude = $_REQUEST['latitude'];
	@$capacity = $_REQUEST['capacity'];
	
	if(empty($mac) || !isset($longitude) || !isset($latitude) || !isset($capacity) ) {
		die("参数错误");
	}

	$my_sqli = new mysqli("localhost", "root", "DD0fc2c31196", "test");
	if($my_sqli->connect_error) {
		die("连接失败:".$my_sqli->connect_error);
	}
	
	$sql = "select * from node where mac='".$mac."';";
	$res = $my_sqli->query($sql);
	$row_cnt = $res->num_rows;
	//echo  "row_cnt=".$row_cnt;
	$res->free();
	if($row_cnt  == 0) {		
		$sql = "insert into node ( mac, longitude, latitude, capacity )
                       values
                       ( '".$mac."', round($longitude, 10), round($latitude, 8), $capacity );";
	} else {
		$sql = "update node 
					set
					longitude=round($longitude, 8), latitude=round($latitude, 8), capacity=$capacity 
					where mac='".$mac."';";
	}
	//echo "<br/>sql:$sql";
	$res = $my_sqli->query($sql);
	if($res < 0) {
		die("操作错误".$my_sqli->error);
	}
	
	echo  "Success";
	
	$my_sqli->close();
?>