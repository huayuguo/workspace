<?php
 
	$path = 'sudo git pull';
	 
	exec($path,$output,$ret);
	
	print_r($output);
	echo "$ret</br>";
	
	system('whoami');
?>