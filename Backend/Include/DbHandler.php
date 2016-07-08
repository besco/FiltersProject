<?php
 
/* Класс с 4мя основными методами работы с базой данных */
class DbHandler {
 
    private $conn;
 
    function __construct() {
        require_once dirname(__FILE__) . '/DbConnect.php';
        $db = new DbConnect();
        $this->conn = $db->connect();
    }
	
     /* ------------- `Users` table method ------------------ */
	 
    public function createUser($name, $email, $password, $phone) {
        require_once 'PassHash.php';
        $response = array();
 
        if (!$this->isUserExists($email)) {
 
            $password_hash = PassHash::hash($password);
            $api_key = $this->generateApiKey();
 
            $stmt = $this->conn->prepare("INSERT INTO `Users`(`name`, `email`, `password_hash`, `api_key`, `phone`) VALUES (?,?,?,?,?)");
            $stmt->bind_param("sssss", $name, $email, $password_hash, $api_key, $phone);
 
            $result = $stmt->execute();
            $stmt->close();
 
            if ($result) {
                return USER_CREATED_SUCCESSFULLY;
            } else {
                return USER_CREATE_FAILED;
            }
        } else {
            return USER_ALREADY_EXISTED;
        }
        return $response;
    }
 
    public function checkLogin($email, $password) {
        require_once 'PassHash.php';
    		$stmt = $this->conn->prepare("SELECT `password_hash` FROM `Users` WHERE `email`= ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->bind_result($password_hash);
        $stmt->store_result();
 
        if ($stmt->num_rows > 0) {
            $stmt->fetch();
            $stmt->close();
            if (PassHash::check_password($password_hash, $password)) {
                return TRUE;
            } else {
                return FALSE;
            }
        } else {
            $stmt->close();
            return FALSE;
        }
    }
 
    private function isUserExists($email) {
        $stmt = $this->conn->prepare("SELECT `id` FROM `Users` WHERE `email`= ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }
 
    public function getUserByEmail($email) {
        $stmt = $this->conn->prepare("SELECT `name`, `email`, `api_key`, `phone`, `status`, `create_date` FROM `Users` WHERE `email`= ?");
        $stmt->bind_param("s", $email);
        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $user;
        } else {
            return NULL;
        }
    }
 
    public function getApiKeyById($user_id) {
        $stmt = $this->conn->prepare("SELECT api_key FROM users WHERE id = ?");
        $stmt->bind_param("i", $user_id);
        if ($stmt->execute()) {
            $api_key = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $api_key;
        } else {
            return NULL;
        }
    }
 
    public function getUserId($api_key) {
        $stmt = $this->conn->prepare("SELECT `id` FROM `Users` WHERE `api_key`= ?");
        $stmt->bind_param("s", $api_key);
        if ($stmt->execute()) {
            $user_id = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $user_id;
        } else {
            return NULL;
        }
    }
 
    public function isValidApiKey($api_key) {
        $stmt = $this->conn->prepare("SELECT `id` FROM `Users` WHERE `api_key`= ?");
        $stmt->bind_param("s", $api_key);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }
 
    private function generateApiKey() {
        return md5(uniqid(rand(), true));
    }	
 
     /* ------------- `Firm` table method ------------------ */
 
    public function createFirm($title, $about, $logo) {        
        $stmt = $this->conn->prepare("INSERT INTO `Firm`(`title`, `about`, `logo`) VALUES (?,?,?)");
        $stmt->bind_param("sss", $title,$about,$logo);
        $result = $stmt->execute();
        $stmt->close();
        if ($result) { return $result; } 
		else { return NULL; } 
	}
 
    public function getAllFirms() {
        $stmt = $this->conn->prepare("SELECT * FROM `Firm`");
        if ($stmt->execute()) {
            $allfirms = $stmt->get_result();
            $stmt->close();
            return $allfirms;
        } else {
            return NULL;
        }
    }
 
    public function getNeedFirm($firm_id) {
        $stmt = $this->conn->prepare("SELECT * FROM `Firm` WHERE `id`=?");
        $stmt->bind_param("i", $firm_id);
        if ($stmt->execute()) {
			$needfirm = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			return $needfirm;
		} else {
			return NULL;
		}		
    }
 
    public function updateFirm($firm_id, $title, $about, $logo) {
        $stmt = $this->conn->prepare("UPDATE `Firm` SET `title`=?,`about`=?,`logo`=? WHERE `id`=?");
        $stmt->bind_param("sssi", $title, $about, $logo, $firm_id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }
 
    public function deleteFirm($firm_id) {
        $stmt = $this->conn->prepare("DELETE FROM `Firm` WHERE `id`=?");
        $stmt->bind_param("i", $firm_id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }  

    /* ------------- `Category_Filters` method --------------- */

    public function createCat($title, $about) {        
        $stmt = $this->conn->prepare("INSERT INTO `Category_Filters`(`title`, `about`) VALUES (?,?)");
        $stmt->bind_param("ss", $title,$about);
        $result = $stmt->execute();
        $stmt->close();
        if ($result) { 
            return $result; 
        } else { 
            return NULL; 
        } 
	}
 
    public function getAllCat() {
        $stmt = $this->conn->prepare("SELECT * FROM `Category_Filters`");
        if ($stmt->execute()) {
            $allcat = $stmt->get_result();
            $stmt->close();
            return $allcat;
        } else {
            return NULL;
        }
    }
 
    public function getNeedCat($cat_id) {
        $stmt = $this->conn->prepare("SELECT * FROM `Category_Filters` WHERE `id`=?");
        $stmt->bind_param("i", $cat_id);
        if ($stmt->execute()) {
			$needcat = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			return $needcat;
		} else {
			return NULL;
		}		
    }
 
    public function updateCat($cat_id, $title, $about) {
        $stmt = $this->conn->prepare("UPDATE `Category_Filters` SET `title`=?,`about`=? WHERE `id`=?");
        $stmt->bind_param("ssi", $title, $about, $cat_id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }
 
    public function deleteCat($cat_id) {
        $stmt = $this->conn->prepare("DELETE FROM `Category_Filters` WHERE `id`=?");
        $stmt->bind_param("i", $cat_id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }  

    /* -----------  `comp_category` method ------------------- */

    public function createComp_Cat($id_firm, $id_cat) {        
        $stmt = $this->conn->prepare("INSERT INTO `comp_category`(`id_firm`, `id_cat`) VALUES (?,?)");
        $stmt->bind_param("ii", $id_firm, $id_cat);
        $result = $stmt->execute();
        $stmt->close();
        if ($result) { return $result; } 
		else { return NULL; } 
	}

    public function deleteComp_Cat($id) {
        $stmt = $this->conn->prepare("DELETE FROM `comp_category` WHERE `id`=?");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }  

    public function deleteAllComp_CatOneFirm($id_firm) {
        $stmt = $this->conn->prepare("DELETE FROM `comp_category` WHERE `id_firm`=?");
        $stmt->bind_param("i", $id_firm);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }  

    public function getAllFirmCat($id_firm) {
        $stmt = $this->conn->prepare("SELECT cf.* FROM `Category_Filters` cf, `comp_category` cc where cc.`id_firm`=? and cc.`id_cat`=cf.`id`");
        $stmt->bind_param("i", $id_firm);
        if ($stmt->execute()) {
            $allFirmCat = $stmt->get_result();
            $stmt->close();
            return $allFirmCat;
        } else {
            return NULL;
        }	
    }
	
	/* ------------- `Filters` table method ------------------ */
	
    public function createFilter($id_firm, $id_cat, $title, $min_title, $about, $price, $logo) {        
        $stmt = $this->conn->prepare("INSERT INTO `Filters`(`id_firm`, `id_cat`, `title`, `min_title`, `about`, `price`, `logo`) VALUES (?,?,?,?,?,?,?)");
        $stmt->bind_param("iisssds", $id_firm, $id_cat, $title, $min_title, $about, $price, $logo);
        $result = $stmt->execute();
        $stmt->close();
        if ($result) { return $result; } 
		else { return NULL; } 
	}

	public function getAllFilters() {
        $stmt = $this->conn->prepare("SELECT * FROM `Filters`");
        if ($stmt->execute()) {
            $allfilters = $stmt->get_result();
            $stmt->close();
            return $allfilters;
        } else {
            return NULL;
        }
    }
	
	public function getAllFirmFilters($id_firm) {
        $stmt = $this->conn->prepare("SELECT * FROM `Filters` WHERE `id_firm`=?");
        $stmt->bind_param("i", $id_firm);
        if ($stmt->execute()) {
			$allfilters = $stmt->get_result();
			$stmt->close();
			return $allfilters;
		} else {
			return NULL;
		}		
    }
	
	public function getAllCatFilters($id_cat) {
        $stmt = $this->conn->prepare("SELECT * FROM `Filters` WHERE `id_cat`=?");
        $stmt->bind_param("i", $id_cat);
        if ($stmt->execute()) {
			$allfilters = $stmt->get_result();
			$stmt->close();
			return $allfilters;
		} else {
			return NULL;
		}		
    }

	public function getOneFilter($id_filter) {
        $stmt = $this->conn->prepare("SELECT * FROM `Filters` WHERE `id`=?");
        $stmt->bind_param("i", $id_filter);
        if ($stmt->execute()) {
			$allfilters = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			return $allfilters;
		} else {
			return NULL;
		}		
    }
	
	public function updateOneFilter($id_filter, $id_firm, $id_cat, $title, $min_title, $about, $price, $logo) {
        $stmt = $this->conn->prepare("UPDATE `Filters` SET `id_firm`=?, `id_cat`=?, `title`=?, `min_title`=?, `about`=?, `price`=?, `logo`=? WHERE `id`=?");
        $stmt->bind_param("iisssdsi", $id_firm, $id_cat, $title, $min_title, $about, $price, $logo, $id_filter);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }
 
    public function deleteFilter($id_filter) {
        $stmt = $this->conn->prepare("DELETE FROM `Filter` WHERE `id`=?");
        $stmt->bind_param("i", $id_filter);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }  
	
	/* ------------- `Moduls` table method ------------------ */
	
    public function createModule($title, $min_title, $about, $price, $live_time, $live_vol, $logo) {        
        $stmt = $this->conn->prepare("INSERT INTO `Moduls`(`title`, `min_title`, `about`, `price`, `live_time`, `live_vol`, `logo`) VALUES (?,?,?,?,?,?,?)");
        $stmt->bind_param("sssdiis", $title, $min_title, $about, $price, $live_time, $live_vol, $logo);
        $result = $stmt->execute();
        $stmt->close();
        if ($result) { return $result; } 
		else { return NULL; } 
	}
	
	public function getOneModule($id_module) {
        $stmt = $this->conn->prepare("SELECT * FROM `Moduls` WHERE `id`=?");
				$stmt->bind_param("i", $id_module);
        if ($stmt->execute()) {
            $moduls = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $moduls;
        } else {
            return NULL;
        }
    }
	
	public function updateOneModule($id_module, $title, $min_title, $about, $price, $live_time, $live_vol, $logo) {
        $stmt = $this->conn->prepare("UPDATE `Moduls` SET `title`=?, `min_title`=?, `about`=?, `price`=?, `live_time`=?, `live_vol`=?, `logo`=? WHERE `id`=?");
        $stmt->bind_param("sssdiisi", $title, $min_title, $about, $price, $live_time, $live_vol, $logo, $id_module);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }
 
    public function deleteModule($id_module) {
        $stmt = $this->conn->prepare("DELETE FROM `Moduls` WHERE `id`=?");
        $stmt->bind_param("i", $id_module);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }  

    /* ------------- `comp_moduls` table method -------------- */

    public function createComp_Moduls($id_filter, $id_module) {        
        $stmt = $this->conn->prepare("INSERT INTO `comp_moduls`(`id_filter`, `id_module`) VALUES (?,?)");
        $stmt->bind_param("ii", $id_filter, $id_module);
        $result = $stmt->execute();
        $stmt->close();
        if ($result) { return $result; } 
		else { return NULL; } 
	}

    public function deleteComp_Moduls($id) {
        $stmt = $this->conn->prepare("DELETE FROM `comp_moduls` WHERE `id`=?");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }  

    public function deleteAllFilterComp_Moduls($id_filter) {
        $stmt = $this->conn->prepare("DELETE FROM `comp_moduls` WHERE `id_filter`=?");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }  

    public function getAllFilterModuls($id_filter) {
        $stmt = $this->conn->prepare("SELECT m.* FROM `Moduls` m, `comp_moduls` cm where cm.`id_filter`=? and cm.`id_module`=m.`id`");
        $stmt->bind_param("i", $id_filter);
        if ($stmt->execute()) {
            $allFilterModuls = $stmt->get_result();
            $stmt->close();
            return $allFilterModuls;
        } else {
            return NULL;
        }
    }
	
	/* ------------- `Use_Filters` table method ------------------ */
	
    public function createUserFilter($id_filter, $id_user, $address, $contact_phone, $date_begin, $date_end) { 
    	  
		$date_b = date("Y-m-d",strtotime($date_begin));
		
		if ($date_end != '') {
			$date_e = date("Y-m-d",strtotime($date_end));
			$stmt = $this->conn->prepare("INSERT INTO `Use_Filters`(`id_filter`, `id_user`, `address`, `contact_phone`, `date_begin`, `date_end`) VALUES (?,?,?,?,?,?)");
        $stmt->bind_param("iissss", $id_filter, $id_user, $address, $contact_phone, $date_b, $date_e);
			}
		else {
			$stmt = $this->conn->prepare("INSERT INTO `Use_Filters`(`id_filter`, `id_user`, `address`, `contact_phone`, `date_begin`) VALUES (?,?,?,?,?)");
        $stmt->bind_param("iisss", $id_filter, $id_user, $address, $contact_phone, $date_b);
			}
        $result = $stmt->execute();      
       	$id_new_filter = mysqli_insert_id($this->conn);
        $stmt->close();
        if ($result) { 
        	return $id_new_filter;
        } 
				else { return NULL; } 
	}
	
	public function getAllUserFilters($id_user) {
        $stmt = $this->conn->prepare("SELECT uf.*, af.`title`, af.`about` FROM `Use_Filters` uf, `Filters` af WHERE `id_user`=? and af.`id`=uf.`id_filter`");
        $stmt->bind_param("i", $id_user);
        if ($stmt->execute()) {
			$allfilters = $stmt->get_result();
			$stmt->close();
			return $allfilters;
		} else {
			return NULL;
		}		
    }
	
	public function updateUserFilter($id_use_filter, $id_filter, $address, $contact_phone, $date_begin, $date_end) {
		$date_b = date("Y-m-d",strtotime($date_begin));
		$date_e = date("Y-m-d",strtotime($date_end));
        $stmt = $this->conn->prepare("UPDATE `Use_Filters` SET `id_filter`=?, `address`=?, `contact_phone`=?, `date_begin`=?, `date_end`=? WHERE `id`=?");
        $stmt->bind_param("issssi", $id_filter, $address, $contact_phone, $date_b, $date_e, $id_use_filter);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }
 
    public function deleteUserFilter($id_filter) {
        $stmt = $this->conn->prepare("DELETE FROM `Use_Filters` WHERE `id`=?");
        $stmt->bind_param("i", $id_filter);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    } 
	
	/* ------------- `Use_Moduls` table method ------------------ */
	
	public function createUseModule($id_module, $id_use_filter, $date_begin, $date_end) {        
		$date_b = date("Y-m-d",strtotime($date_begin));
		$date_e = date("Y-m-d",strtotime($date_end));
    	$stmt = $this->conn->prepare("INSERT INTO `Use_Moduls`(`id_module`, `id_use_filter`, `date_begin`, `date_end`) VALUES (?,?,?,?)");
        $stmt->bind_param("iiss", $id_module, $id_use_filter, $date_b, $date_e);
        $result = $stmt->execute();
        $stmt->close();
        if ($result) { return $result; } 
		else { return NULL; } 
	}
	
	public function getAllUseFilterModuls($id_use_filter) {
        $stmt = $this->conn->prepare("SELECT um.*, am.`title`, am.`about` FROM `Use_Moduls` um, `Moduls` am WHERE `id_use_filter`=? and am.`id`=um.`id_module`");
        $stmt->bind_param("i", $id_use_filter);
        if ($stmt->execute()) {
			$allfilters = $stmt->get_result();
			$stmt->close();
			return $allfilters;
		} else {
			return NULL;
		}		
    }	
	
	public function deleteFilterModule($id_module) {
        $stmt = $this->conn->prepare("DELETE FROM `Use_Moduls` WHERE `id`=?");
        $stmt->bind_param("i", $id_module);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }	
 
}
 
?>