<?php
/*
class Config {
	public   $WSDL = "http://192.168.28.101:8124/soap-wsdl/syracuse/collaboration/syracuse/CAdxWebServiceXmlCC?wsdl";
	public   $CODE_LANG = "ENG";
	public   $CODE_USER = "WSTERM";
	public   $PASSWORD = "WsT3RM";
	public   $POOL_ALIAS = "SPA";
	public   $REQUEST_CONFIG = "adxwss.trace.on=off&adxwss.trace.size=16384&adonix.trace.on=off&adonix.trace.level=3&adonix.trace.size=8";
}
*/
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
'poolAlias' => "WSTEST",
'requestConfig' => "adxwss.trace.on=off&adxwss.trace.size=16384&adonix.trace.on=off&adonix.trace.level=3&adonix.trace.size=8&adxwss.optreturn=JSON"
); 
//adxwss.trace.on=on&amp;adxwss.beautify=true&amp;adxwss.optreturn=JSON
$xmlInput=
'{
"GRP1":{
"WSIOF":"OFFAM210300009",
"WSINETIQ":"1",
"WSILECDAT":"16/06/2021",
"WSILECHOR":"15:10:10"
}
}';

$result = $soapClient->run($callContext,"YWSLECET1",$xmlInput);
echo $result->resultXml;

// store the returned XML string
//$xml = simplexml_load_string($result->resultXml);
//echo $xml;
//echo $result->status; //1 ok, 0 error

?>