<?php
//Create the client object
$soapclient = new SoapClient('http://192.168.28.101:8124/soap-wsdl/syracuse/collaboration/syracuse/CAdxWebServiceXmlCC?wsdl');

//Use the functions of the client, the params of the function are in 
//the associative array
$params = array('CODE_LANG ' => 'ENG', 'CODE_USER' => 'WSTERM', 'PASSWORD' => 'WsT3RM', 'POOL_ALIAS' => 'WSLIVE', 'REQUEST_CONFIG' => 'adxwss.trace.on=off&adxwss.trace.size=16384&adonix.trace.on=off&adonix.trace.level=3&adonix.trace.size=8');
$response = $soapclient->read($params);

var_dump($response);

// Get the Cities By Country
//$param = array('CountryName' => 'Spain');
//$response = $soapclient->getCitiesByCountry($param);

var_dump($response);
?>