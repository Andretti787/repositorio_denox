<?php
$wsdl = "http://192.168.28.101:8124/soap-wsdl/syracuse/collaboration/syracuse/CAdxWebServiceXmlCC?wsdl";

$optionsAuth = Array (
'cache_wsdl' => WSDL_CACHE_NONE,
'login' => "WSTERM",
'password' => "WsT3RM",
'trace' => 0
);

$soapClient = new SoapClient ( $wsdl, $optionsAuth );
$callContext = array (
'codeLang' => "SPA",
'poolAlias' => "WSLIVE",
'requestConfig' => "adxwss.trace.on=off&adxwss.trace.size=16384&adonix.trace.on=off&adonix.trace.level=3&adonix.trace.size=8&adxwss.optreturn=JSON"
); 
?>