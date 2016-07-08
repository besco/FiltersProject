<?php 
/* Класс конекта к базе данных, инфа о базе в Config.php */
class DbConnect {
    private $conn;
    function __construct() {        
    } 
    function connect() {
        include_once dirname(__FILE__) . '/Config.php';
        $this->conn = new mysqli(DB_HOST, DB_USERNAME, DB_PASSWORD, DB_NAME);
        if (mysqli_connect_errno()) {
            echo "Ошибка подключения к базе данных: " . mysqli_connect_error();
        } 
		$this->conn->set_charset("utf8");
        return $this->conn;
    } 
} 
?>