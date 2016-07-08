<?php
 
require_once '../Include/DbHandler.php';
require '../libs/Slim/Slim.php';
 
\Slim\Slim::registerAutoloader();
 
$app = new \Slim\Slim(); 
$app->contentType('text/html; charset=utf-8'); 

$id_user = NULL;

function verifyRequiredParams($required_fields) {
    $error = false;
    $error_fields = "";
    $request_params = array();
    $request_params = $_REQUEST;
    if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
        $app = \Slim\Slim::getInstance();
        parse_str($app->request()->getBody(), $request_params);
    }
    foreach ($required_fields as $field) {
        if (!isset($request_params[$field]) || strlen(trim($request_params[$field])) <= 0) {
            $error = true;
            $error_fields .= $field . ', ';
        }
    }
 
    if ($error) {
        $response = array();
        $app = \Slim\Slim::getInstance();
        $response["error"] = true;
        $response["message"] = 'Обязательно к заполнению: ' . substr($error_fields, 0, -2);
        echoRespnse(400, $response);
        $app->stop();
    }
}

function validateEmail($email) {
    $app = \Slim\Slim::getInstance();
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response["error"] = true;
        $response["message"] = 'Email address is not valid';
        echoRespnse(400, $response);
        $app->stop();
    }
}
 
function echoRespnse($status_code, $response) {
    $app = \Slim\Slim::getInstance();
    $app->status($status_code);
    $app->contentType('application/json');
    echo json_encode($response, JSON_UNESCAPED_UNICODE); 
}

/* ---------------------- РАБОТА С ЮЗВЕРЯМИ -------------------------- */

$app->post('/register', function() use ($app) {
            verifyRequiredParams(array('name', 'email', 'password'));
 
            $response = array();
 
            $name     = $app->request->post('name')    ;
            $email    = $app->request->post('email')   ;
            $password = $app->request->post('password');
            $phone    = $app->request->post('phone')   ;
 
            validateEmail($email);
 
            $db = new DbHandler();
            $res = $db->createUser($name, $email, $password, $phone);
 
            if ($res == USER_CREATED_SUCCESSFULLY) {
                $response["error"] = false;
                $response["message"] = "Регистрация прошла успешна.";
                echoRespnse(201, $response);
            } else if ($res == USER_CREATE_FAILED) {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при регистрации пользователя.";
                echoRespnse(200, $response);
            } else if ($res == USER_ALREADY_EXISTED) {
                $response["error"] = true;
                $response["message"] = "Извините, этот e-mail уже зарегистрирован в системе.";
                echoRespnse(200, $response);
            }
        });
		
$app->post('/login', function() use ($app) {
            verifyRequiredParams(array('email', 'password'));
 
            $email    = $app->request()->post('email');
            $password = $app->request()->post('password');
            $response = array();
 
            $db = new DbHandler();
            if ($db->checkLogin($email, $password)) {
                $user = $db->getUserByEmail($email);
                if ($user != NULL) {
                    $response["error"]       = false;
                    $response['name']        = $user['name']       ;
                    $response['email']       = $user['email']      ;
                    $response['apiKey']      = $user['api_key']    ;
                    $response['phone']       = $user['phone']      ;
                    $response['status']      = $user['status']     ;
                    $response['create_date'] = $user['create_date'];
                } else {
                    $response['error'] = true;
                    $response['message'] = "Какая то не понятная ошибка, которой не должно быть.";
                }
            } else {
                $response['error'] = true;
                $response['message'] = 'Логин или пароль ведены не верно, попробуйте снова';
            }
            echoRespnse(200, $response);
        });
		
function authenticate(\Slim\Route $route) {
    $headers = apache_request_headers();
    $response = array();
    $app = \Slim\Slim::getInstance();
 
    if (isset($headers['Authorization'])) {
        $db = new DbHandler();
 
        $api_key = $headers['Authorization'];
        if (!$db->isValidApiKey($api_key)) {
            $response["error"] = true;
            $response["message"] = "Access Denied. Invalid Api key";
            echoRespnse(401, $response);
            $app->stop();
        } else {
            global $id_user;
            $user = $db->getUserId($api_key);
            if ($user != NULL)
                $id_user = $user["id"];
        }
    } else {
        $response["error"] = true;
        $response["message"] = "Api key is misssing";
        echoRespnse(400, $response);
        $app->stop();
    }
}

/* -------------------------- ДЛЯ ФИРМ ------------------------------- */

$app->post('/firms', 'authenticate', function() use ($app) {
	
            verifyRequiredParams(array('title'));
 
            $response = array();
            $title = $app->request->post('title');
            $about = $app->request->post('about');
            $logo  = $app->request->post('logo') ;

            $db = new DbHandler();
 
            $firm_id = $db->createFirm($title, $about, $logo);
 
            if ($firm_id != NULL) {
                $response["error"]   = false;
                $response["message"] = "Создали фирму производителя.";
                $response["firm_id"] = $firm_id;
            } else {
                $response["error"]   = true;
                $response["message"] = "Ошибка при создании фирмы производителя. Попробуйте снова.";
            }
            echoRespnse(201, $response);
        });

$app->get('/firms', 'authenticate', function() {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getAllFirms();
 
            $response["error"] = false;
            $response["firms"] = array();
 
            while ($firm = $result->fetch_assoc()) {
                $tmp          = array();
                $tmp["id"]    = $firm["id"];
                $tmp["title"] = $firm["title"];
                $tmp["about"] = $firm["about"];
                $tmp["logo"]  = $firm["logo"];
                array_push($response["firms"], $tmp);
            } 
            echoRespnse(200, $response);
        });		
		
$app->get('/firms/:id', 'authenticate', function($firm_id) {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getNeedFirm($firm_id);
 
            if ($result != NULL) {
                $response["error"] = false;
                $response["id"] = $result["id"];
                $response["title"] = $result["title"];
                $response["about"] = $result["about"];
                $response["logo"] = $result["logo"];
                echoRespnse(200, $response);
            } else {
                $response["error"] = true;
                $response["message"] = "По запрашиваемому полю ничего не найдено.";
                echoRespnse(404, $response);
            }
        });

$app->put('/firms/:id', 'authenticate', function($firm_id) use($app) {
 
            $title = $app->request->put('title');
            $about = $app->request->put('about');
            $logo  = $app->request->put('logo') ;
 
            $db = new DbHandler();
            $response = array();
 
            $result = $db->updateFirm($firm_id, $title, $about, $logo);
            if ($result) {
                $response["error"] = false;
                $response["message"] = "Данные изменены.";
            } else {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при изменении данных.";
            }
            echoRespnse(200, $response);
        });
		
$app->delete('/firms/:id', 'authenticate', function($firm_id) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteFirm($firm_id);
            if ($result) {
                $response["error"] = false;
                $response["message"] = "Удалено.";
            } else {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });

/* -------------------- КАТЕГОРИИ ФИЛЬТРОВ ------------------------- */

$app->post('/cats', 'authenticate', function() use ($app) {
	
            verifyRequiredParams(array('title'));
 
            $response = array();
            $title = $app->request->post('title');
            $about = $app->request->post('about');

            $db = new DbHandler();
 
            $cat_id = $db->createCat($title, $about);
 
            if ($cat_id != NULL) {
                $response["error"]   = false;
                $response["message"] = "Создали категорию фильтра.";
                $response["cat_id"]  = $cat_id;
            } else {
                $response["error"]   = true;
                $response["message"] = "Ошибка при создании категории фильтра. Попробуйте снова.";
            }
            echoRespnse(201, $response);
        });

$app->get('/cats', 'authenticate', function() {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getAllCat();
 
            $response["error"] = false;
            $response["category_filters"] = array();
 
            while ($category_filters = $result->fetch_assoc()) {
                $tmp = array();
                $tmp["id"]    = $category_filters["id"];
                $tmp["title"] = $category_filters["title"];
                $tmp["about"] = $category_filters["about"];
                array_push($response["category_filters"], $tmp);
            } 
            echoRespnse(200, $response);
        });		
		
$app->get('/cats/:id', 'authenticate', function($cat_id) {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getNeedCat($cat_id);
 
            if ($result != NULL) {
                $response["error"] = false;
                $response["id"]    = $result["id"];
                $response["title"] = $result["title"];
                $response["about"] = $result["about"];
                echoRespnse(200, $response);
            } else {
                $response["error"] = true;
                $response["message"] = "По запрашиваемому полю ничего не найдено.";
                echoRespnse(404, $response);
            }
        });

$app->put('/cats/:id', 'authenticate', function($cat_id) use($app) {
 
            $title = $app->request->put('title');
            $about = $app->request->put('about');
 
            $db = new DbHandler();
            $response = array();
 
            $result = $db->updateCat($cat_id, $title, $about);
            if ($result) {
                $response["error"]   = false;
                $response["message"] = "Данные изменены.";
            } else {
                $response["error"]   = true;
                $response["message"] = "Возникла ошибка при изменении данных.";
            }
            echoRespnse(200, $response);
        });
		
$app->delete('/cats/:id', 'authenticate', function($cat_id) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteCat($cat_id);
            if ($result) {
                $response["error"]   = false;
                $response["message"] = "Удалено.";
            } else {
                $response["error"]   = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });

/* ---------------- У КАЖДОЙ ФИРМЫ СВОИ КАТЕГОРИИ ФИЛЬТРОВ, МОЖЕТ БЫТЬ ОДИНАКОВЫЕ, ПОЭТОМУ СВЯЗУЮЩАЯ ТАБЛИЦА ------------------ */

$app->post('/comp_cats', 'authenticate', function() use ($app) {
 
            $response = array();
            $id_firm  = $app->request->post('id_firm');
            $id_cat   = $app->request->post('id_cat');

            $db = new DbHandler();
 
            $comp_cat_id = $db->createComp_Cat($id_firm, $id_cat);
 
            if ($comp_cat_id != NULL) {
                $response["error"]        = false;
                $response["message"]      = "Привязали к фирме категорию фильра.";
                $response["comp_cat_id"]  = $comp_cat_id;
            } else {
                $response["error"]   = true;
                $response["message"] = "Ошибка при привязке категории к фирме. Попробуйте снова.";
            }
            echoRespnse(201, $response);
        });

$app->delete('/comp_cats/:id', 'authenticate', function($comp_cat_id) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteComp_Cat($comp_cat_id);
            if ($result) {
                $response["error"]   = false;
                $response["message"] = "Убрали у фирмы данную категорию фильтров.";
            } else {
                $response["error"]   = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });

$app->delete('/comp_cats_firm/:id', 'authenticate', function($id_firm) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteAllComp_CatOneFirm($id_firm);
            if ($result) {
                $response["error"]   = false;
                $response["message"] = "Убрали у фирмы все категории.";
            } else {
                $response["error"]   = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });

/* получить список всех категорий фирмы */
$app->get('/firm_cats/:id', 'authenticate', function($id_firm) {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getAllFirmCat($id_firm);
 
            $response["error"] = false;
            $response["category_filters"] = array();

            while ($category_filters = $result->fetch_assoc()) {
                $tmp = array();
                $tmp["id"]    = $category_filters["id"];
                $tmp["title"] = $category_filters["title"];
                $tmp["about"] = $category_filters["about"];
                array_push($response["category_filters"], $tmp);
            } 
            echoRespnse(200, $response);            
        });
		
/* -------------------------- ДЛЯ ФИЛЬТРОВ ------------------------------- */

$app->post('/filters', 'authenticate', function() use ($app) {
	
            verifyRequiredParams(array('id_firm', 'id_cat', 'title'));
 
            $response  = array();
            $id_firm   = $app->request->post('id_firm')   ;
            $id_cat    = $app->request->post('id_cat')    ;
            $title     = $app->request->post('title')     ;
            $min_title = $app->request->post('min_title') ;
            $about     = $app->request->post('about')     ;
            $price     = $app->request->post('price')     ;
            $logo      = $app->request->post('logo')      ;

            $db = new DbHandler();
 
            $id_filter = $db->createFilter($id_firm, $id_cat, $title, $min_title, $about, $price, $logo);
 
            if ($id_filter != NULL) {
                $response["error"]   = false;
                $response["message"] = "Создали фильтр.";
                $response["firm_id"] = $id_filter;
            } else {
                $response["error"]   = true;
                $response["message"] = "Ошибка при создании фильтра. Попробуйте снова.";
            }
            echoRespnse(201, $response);
        });

$app->get('/filters', 'authenticate', function() {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getAllFilters();
 
            $response["error"]   = false;
            $response["filters"] = array();
 
            while ($filter = $result->fetch_assoc()) {
                $tmp = array();
                $tmp["id"]        = $filter["id"]        ;
                $tmp["id_firm"]   = $filter["id_firm"]   ;
                $tmp["id_cat"]    = $filter["id_cat"]    ;
                $tmp["title"]     = $filter["title"]     ;
                $tmp["min_title"] = $filter["min_title"] ;
                $tmp["about"]     = $filter["about"]     ;
                $tmp["price"]     = $filter["price"]     ;
                $tmp["logo"]      = $filter["logo"]      ;
                array_push($response["filters"], $tmp);
            } 
            echoRespnse(200, $response);
        });		
		
$app->get('/firm_filters/:id', 'authenticate', function($id_firm) {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getAllFirmFilters($id_firm);
 
            $response["error"]   = false;
            $response["filters"] = array();
 
            while ($filter = $result->fetch_assoc()) {
                $tmp = array();
                $tmp["id"]        = $filter["id"]        ;
                $tmp["id_firm"]   = $filter["id_firm"]   ;
                $tmp["id_cat"]    = $filter["id_cat"]    ;
                $tmp["title"]     = $filter["title"]     ;
                $tmp["min_title"] = $filter["min_title"] ;
                $tmp["about"]     = $filter["about"]     ;
                $tmp["price"]     = $filter["price"]     ;
                $tmp["logo"]      = $filter["logo"]      ;
                array_push($response["filters"], $tmp);
            } 
            echoRespnse(200, $response);
        });	

$app->get('/cat_filters/:id', 'authenticate', function($id_cat) {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getAllCatFilters($id_cat);
 
            $response["error"]   = false;
            $response["filters"] = array();
 
            while ($filter = $result->fetch_assoc()) {
                $tmp = array();
                $tmp["id"]        = $filter["id"]        ;
                $tmp["id_firm"]   = $filter["id_firm"]   ;
                $tmp["id_cat"]    = $filter["id_cat"]    ;
                $tmp["title"]     = $filter["title"]     ;
                $tmp["min_title"] = $filter["min_title"] ;
                $tmp["about"]     = $filter["about"]     ;
                $tmp["price"]     = $filter["price"]     ;
                $tmp["logo"]      = $filter["logo"]      ;
                array_push($response["filters"], $tmp);
            } 
            echoRespnse(200, $response);
        });	

$app->get('/filters/one/:id', 'authenticate', function($id_filter) {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getOneFilter($id_filter);
 
            if ($result != NULL) {
                $response["error"]     = false;
                $response["id"]        = $result["id"]        ;
                $response["id_firm"]   = $result["id_firm"]   ;
                $response["id_cat"]    = $result["id_cat"]    ;
                $response["title"]     = $result["title"]     ;
                $response["min_title"] = $result["min_title"] ;
                $response["about"]     = $result["about"]     ;
                $response["price"]     = $result["price"]     ;
                $response["logo"]      = $result["logo"]      ;
                echoRespnse(200, $response);
            } else {
                $response["error"] = true;
                $response["message"] = "По запрашиваемому полю ничего не найдено.";
                echoRespnse(404, $response);
            }
        });		
		
$app->put('/filters/:id', 'authenticate', function($id_filter) use($app) {
 
            $id_firm   = $app->request->put('id_firm')   ;
            $id_cat    = $app->request->put('id_cat')    ;
            $title     = $app->request->put('title')     ;
            $min_title = $app->request->put('min_title') ;
            $about     = $app->request->put('about')     ;
            $price     = $app->request->put('price')     ;
            $logo      = $app->request->put('logo')      ;
 
            $db = new DbHandler();
            $response = array();
 
            $result = $db->updateOneFilter($id_filter, $id_firm, $id_cat, $title, $min_title, $about, $price, $logo);
            if ($result) {
                $response["error"] = false;
                $response["message"] = "Данные изменены.";
            } else {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при изменении данных.";
            }
            echoRespnse(200, $response);
        });
		
$app->delete('/filters/:id', 'authenticate', function($id_filter) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteFilter($id_filter);
            if ($result) {
                $response["error"] = false;
                $response["message"] = "Удалено.";
            } else {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });

/* -------------------------- ДЛЯ КАРТРИДЖЕЙ ----------------------------- */

$app->post('/moduls', 'authenticate', function() use ($app) {
	
            verifyRequiredParams(array('title'));
 
            $response  = array();
            $title     = $app->request->post('title')     ;
            $min_title = $app->request->post('min_title') ;
            $about     = $app->request->post('about')     ;
			$price     = $app->request->post('price')     ;
            $live_time = $app->request->post('live_time') ;
            $live_vol  = $app->request->post('live_vol')  ;
            $logo      = $app->request->post('logo')      ;

            $db = new DbHandler();
 
            $id_filter = $db->createModule($title, $min_title, $about, $price, $live_time, $live_vol, $logo);
 
            if ($id_filter != NULL) {
                $response["error"]   = false;
                $response["message"] = "Создали картридж.";
                $response["firm_id"] = $id_filter;
            } else {
                $response["error"]   = true;
                $response["message"] = "Ошибка при создании картриджа. Попробуйте снова.";
            }
            echoRespnse(201, $response);
        });
		
$app->get('/moduls/one/:id', 'authenticate', function($id_module) {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getOneModule($id_module);
 
            if ($result != NULL) {
                $response["error"]     = false;
                $response["id"]        = $result["id"]        ;
                $response["title"]     = $result["title"]     ;
                $response["min_title"] = $result["min_title"] ;
                $response["about"]     = $result["about"]     ;
                $response["price"]     = $result["price"]     ;
                $response["live_time"] = $result["live_time"] ;
                $response["live_vol"]  = $result["live_vol"]  ;
                $response["logo"]      = $result["logo"]      ;
                echoRespnse(200, $response);
            } else {
                $response["error"] = true;
                $response["message"] = "По запрашиваемому полю ничего не найдено.";
                echoRespnse(404, $response);
            }
        });	

$app->put('/moduls/:id', 'authenticate', function($id_module) use($app) {
 
            $title     = $app->request->put('title')     ;
            $min_title = $app->request->put('min_title') ;
            $about     = $app->request->put('about')     ;
            $price     = $app->request->put('price')     ;
            $live_time = $app->request->put('live_time') ;
            $live_vol  = $app->request->put('live_vol')  ;
            $logo      = $app->request->put('logo')      ;
 
            $db = new DbHandler();
            $response = array();
 
            $result = $db->updateOneModule($id_module, $title, $min_title, $about, $price, $live_time, $live_vol, $logo);
            if ($result) {
                $response["error"] = false;
                $response["message"] = "Данные изменены.";
            } else {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при изменении данных.";
            }
            echoRespnse(200, $response);
        });
		
$app->delete('/moduls/:id', 'authenticate', function($id_module) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteModule($id_module);
            if ($result) {
                $response["error"] = false;
                $response["message"] = "Удалено.";
            } else {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });
/* ------------ ОДИН КАРТРИДЖ МОЖЕТ ПОДХОДИТЬ НЕСКОЛЬКИМ ФИЛЬТРАМ, ПОЭТОМУ СВЯЗУЮЩАЯ ТАБЛИЦА ----------------- */

$app->post('/comp_moduls', 'authenticate', function() use ($app) {
 
            $response  = array();
            $id_filter = $app->request->post('id_filter');
            $id_module = $app->request->post('id_module');

            $db = new DbHandler();
 
            $comp_module_id = $db->createComp_Moduls($id_filter, $id_module) ;
 
            if ($comp_module_id != NULL) {
                $response["error"]          = false;
                $response["message"]        = "Привязали картридж к фильтру.";
                $response["comp_module_id"] = $comp_module_id;
            } else {
                $response["error"]   = true;
                $response["message"] = "Ошибка при привязке картриджa к фильтру. Попробуйте снова.";
            }
            echoRespnse(201, $response);
        });

$app->delete('/comp_moduls/:id', 'authenticate', function($id) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteComp_Moduls($id);
            if ($result) {
                $response["error"]   = false;
                $response["message"] = "Убрали у фильтра данный картридж.";
            } else {
                $response["error"]   = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });

$app->delete('/comp_moduls_filter/:id', 'authenticate', function($id_filter) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteAllFilterComp_Moduls($id_filter);
            if ($result) {
                $response["error"]   = false;
                $response["message"] = "Убрали у фильтра все подходящие картриджи.";
            } else {
                $response["error"]   = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });

/* получить список всех картриджей, подходящих данному фильтру */
$app->get('/filter_moduls/:id', 'authenticate', function($id_filter) {
	
            $response = array();
            $db = new DbHandler();
 
            $result = $db->getAllFilterModuls($id_filter);
 
            $response["error"]   = false;
            $response["filter_moduls"] = array();
 
            while ($filter_moduls = $result->fetch_assoc()) {
                $tmp = array();
                $tmp["id"]        = $filter_moduls["id"]        ;
                $tmp["title"]     = $filter_moduls["title"]     ;
                $tmp["min_title"] = $filter_moduls["min_title"] ;
                $tmp["about"]     = $filter_moduls["about"]     ;
                $tmp["price"]     = $filter_moduls["price"]     ;
                $tmp["live_time"] = $filter_moduls["live_time"]     ;
                $tmp["live_vol"]  = $filter_moduls["live_vol"]     ;
                $tmp["logo"]      = $filter_moduls["logo"]      ;
                array_push($response["filter_moduls"], $tmp);
            } 
            echoRespnse(200, $response);
        });
		
/* -------------------------- ДЛЯ УСТАНОВЛЕННЫХ У ЮЗЕРА ФИЛЬТРОВ ----------------------------- */

$app->post('/usefilters', 'authenticate', function() use ($app) {

            global $id_user;
	
            verifyRequiredParams(array('id_filter','date_begin','address'));
 
            $response      = array();
            $id_filter     = $app->request->post('id_filter')    ;
            $address       = $app->request->post('address')      ;
            $contact_phone = $app->request->post('contact_phone');
						$date_begin    = $app->request->post('date_begin')   ;
						$date_end      = $app->request->post('date_end')     ;
			
            $db = new DbHandler();
 
            $id_use_filter = $db->createUserFilter($id_filter, $id_user, $address, $contact_phone, $date_begin, $date_end) ;
 
            if ($id_use_filter > 0) {
                $response["error"]         = false;
                $response["id_use_filter"] = $id_use_filter;
                $response["message"]       = "Создали пользователю фильтр.";
            } else {
                $response["error"]   = true;
                $response["id_use_filter"] = $id_use_filter;
                $response["message"] = "Ошибка при создании пользователю фильтра. Попробуйте снова.";
            }
            echoRespnse(201, $response);
        });
		
$app->get('/usefilters', 'authenticate', function() {
	
            $response = array();
            global $id_user;

            $db = new DbHandler();
			
            $result = $db->getAllUserFilters($id_user);
 
            $response["error"]      = false;
            $response["usefilters"] = array();
 
            while ($filter = $result->fetch_assoc()) {
                $tmp = array();
                $tmp["id"]            = $filter["id"]           ;
                $tmp["id_filter"]     = $filter["id_filter"]    ;
                $tmp["id_user"]       = $filter["id_user"]      ;
                $tmp["address"]       = $filter["address"]      ;
                $tmp["contact_phone"] = $filter["contact_phone"];
                $tmp["date_begin"]    = $filter["date_begin"]   ;
                $tmp["date_end"]      = $filter["date_end"]     ;
                $tmp["create_date"]   = $filter["create_date"]  ;
                $tmp["title"]         = $filter["title"]        ;
                $tmp["about"]         = $filter["about"]        ;
                array_push($response["usefilters"], $tmp)       ;
            } 
            echoRespnse(200, $response);
        });	
		
$app->put('/usefilters/:id', 'authenticate', function($id_use_filter) use($app) {
 
            $id_filter     = $app->request->put('id_filter')    ;
            $address       = $app->request->put('address')      ;
            $contact_phone = $app->request->put('contact_phone');
            $date_begin    = $app->request->put('date_begin')   ;
            $date_end      = $app->request->put('date_end')     ;
 
            $db = new DbHandler();
            $response = array();
 
            $result = $db->updateUserFilter($id_use_filter, $id_filter, $address, $contact_phone, $date_begin, $date_end);
            if ($result) {
                $response["error"] = false;
                $response["message"] = "Данные изменены.";
            } else {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при изменении данных.";
            }
            echoRespnse(200, $response);
        });
		
$app->delete('/usefilters/:id', 'authenticate', function($id_filter) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteUserFilter($id_filter);
            if ($result) {
                $response["error"] = false;
                $response["message"] = "Удалено.";
            } else {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });

/* -------------------------- ДЛЯ УСТАНОВЛЕННЫХ У ЮЗЕРА ФИЛЬТРА КАРТРИДЖЕЙ ----------------------------- */

$app->post('/usemoduls', 'authenticate', function() use ($app) {
	
            verifyRequiredParams(array('id_module','id_use_filter','date_begin'));
 
            $response      = array();
            $id_module     = $app->request->post('id_module')     ;
            $id_use_filter = $app->request->post('id_use_filter') ;
		      	$date_begin    = $app->request->post('date_begin')    ;
            $date_end      = $app->request->post('date_end')      ;
			
            $db = new DbHandler();
 
            $id_filter = $db->createUseModule($id_module, $id_use_filter, $date_begin, $date_end);
 
            if ($id_filter != NULL) {
                $response["error"]   = false;
                $response["message"] = "Создали установленному фильтру картридж.";
            } else {
                $response["error"]   = true;
                $response["message"] = "Ошибка при создании установленному фильтру картриджа. Попробуйте снова.";
            }
            echoRespnse(201, $response);
        });
		
$app->get('/usemoduls/:id', 'authenticate', function($id_use_filter) {
	
            $response = array();

            $db = new DbHandler();
			
            $result = $db->getAllUseFilterModuls($id_use_filter);
 
            $response["error"]   = false;
            $response["usemoduls"] = array();
 
            while ($filter = $result->fetch_assoc()) {
                $tmp = array();
                $tmp["id"]            = $filter["id"]           ;
                $tmp["id_module"]     = $filter["id_module"]    ;
                $tmp["id_use_filter"] = $filter["id_use_filter"];
                $tmp["date_begin"]    = $filter["date_begin"]   ;
                $tmp["date_end"]      = $filter["date_end"]     ;
                $tmp["create_date"]   = $filter["create_date"]  ;
                $tmp["title"]         = $filter["title"]        ;
                $tmp["about"]         = $filter["about"]        ;

                array_push($response["usemoduls"], $tmp);
            } 
            echoRespnse(200, $response);
        });
		
$app->delete('/usemoduls/:id', 'authenticate', function($id_module) use($app) {
 
            $db = new DbHandler();
            $response = array();
            $result = $db->deleteFilterModule($id_module);
            if ($result) {
                $response["error"] = false;
                $response["message"] = "Удалено.";
            } else {
                $response["error"] = true;
                $response["message"] = "Возникла ошибка при удалении данных.";
            }
            echoRespnse(200, $response);
        });
		
$app->run();

?>