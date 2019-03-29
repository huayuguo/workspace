<?php
	//require_once()
	header("Content-Type: text/html; charset=UTF-8");
	
	/**
	 * 接受用户登陆时提交的验证码
	 */
	session_start();
	
	$my_sqli = new mysqli("localhost", "root", "DD0fc2c31196", "test");
	if($my_sqli->connect_error) {
		die("Error:".$my_sqli->connect_error);
	}
	
	if($_POST['type'] == "register") {
		$name = $_POST["name"];
		$password = $_POST["password"];	
		
		if(empty($name) || empty($password)) {
			echo "Error:user name or password wrong";
			goto error_out;
		}
		
		$sql = "select * from user where name='".$name."';";
		$res = $my_sqli->query($sql);
		$row_cnt = $res->num_rows;
		$res->free();
		if($row_cnt > 0) {
			echo "该用户名已被使用";
			goto error_out;
		}	
		
		$sql = "insert into user (name, password) values
                       ('".$name."', md5('".$password."'));";
		$res = $my_sqli->query($sql);
		if(!$res) {
			echo "Error:".$my_sqli->error;
		}
		echo "Success:PHPSESSID=".session_id();
		$_SESSION["login"] = "ok";
	} else if ($_POST['type'] == "login"){
		$name = $_POST["name"];
		$password = $_POST["password"];
		$sql = "select password from user where name='".$name."';";
		$res = $my_sqli->query($sql);
		if($res) {
			$row = $res->fetch_row();
			$res->free();
		} else  {
			echo "Error:user not exit";
			goto error_out;
		}
		
		if($row[0] != md5($password)) {
			echo "Error:password not match";
			goto error_out;
		}
		echo "Success:PHPSESSID=".session_id();
		$_SESSION["login"] = "ok";
	}

error_out:	
	$my_sqli->close();
?>