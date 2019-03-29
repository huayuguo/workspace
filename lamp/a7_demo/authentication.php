<?php
	//require_once()
	header("Content-Type: text/html; charset=UTF-8");
	
	/**
	 * 接受用户登陆时提交的验证码
	 */
	session_start();
	//1. 获取到用户提交的验证码
	$captcha = $_POST["captcha"];
	//2. 将session中的验证码和用户提交的验证码进行核对,当成功时提示验证码正确，并销毁之前的session值,不成功则重新提交
	if(strtolower($_SESSION["captcha"]) == strtolower($captcha)){
		//echo "验证码正确!";
		$_SESSION["captcha"] = "";
	}else{
		die("验证码提交不正确!");
	}
	
	$my_sqli = new mysqli("localhost", "root", "DD0fc2c31196", "test");
	if($my_sqli->connect_error) {
		die("连接失败:".$my_sqli->connect_error);
	}
	
	if($_POST['type'] == "register") {
		$name = $_POST["name"];
		$password = $_POST["password"];	
		
		if(empty($name) || empty($password)) {
			echo "用户名或密码输入有误";
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
			echo "数据库操作失败".$my_sqli->error;
		}
		echo "注册ok";
		echo "Success:PHPSESSID=".session_id();
		$_SESSION["login"] = "ok";
		header("Location: node_view.php");
	} else if ($_POST['type'] == "login"){
		$name = $_POST["name"];
		$password = $_POST["password"];
		$sql = "select password from user where name='".$name."';";
		$res = $my_sqli->query($sql);
		if($res) {
			$row = $res->fetch_row();
			$res->free();
		} else  {
			echo "密码错误";
			goto error_out;
		}
		
		if($row[0] != md5($password)) {
			echo "密码错误";
			goto error_out;
		}
		echo "登录ok";
		echo "Success:PHPSESSID=".session_id();
		$_SESSION["login"] = "ok";
		header("Location: node_view.php");
	}

error_out:	
	$my_sqli->close();
?>