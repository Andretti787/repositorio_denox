<?php
//echo "http://".$_SERVER['HTTP_HOST'].$_SERVER['REQUEST_URI'];
$error=0;
if (!empty($_GET)){
	if (isset($_GET['consumo'])){
		$consumo=$_GET['consumo']; //PPPPPPPPLOTLLLLLLLLLSSS
		//$codigoarticulo=substr($envasado, 0, 8); 
		//echo strlen($_GET['consumo']);
		
		$porciones = explode("LOT", $consumo);
		$codigoarticulo = $porciones[0]; // porción1
		
		
		$restante = str_replace($codigoarticulo.'LOT', "", $consumo);
		
		//$restante= $porciones[1]; // porción2

		//$separadorlote=substr($envasado, 8, 3); 
		//$lote=substr($envasado, 11, 9); 
		
		$lote= substr($restante, 0, -3);
		
		//$sublote=substr($envasado, 20, 3); 
		
		//$sublote = substr($restante, -3);
	
		$porciones2 = explode("S", $restante);
		
		if (sizeof($porciones2)<2){
			echo '<br/><div align="center"><h2>El formato de los datos introducidos no es correcto</h2></div>';
			exit();
		}
		
		
		$lote = $porciones2[0];
		$sublote = $porciones2[1]; // porción1
		
		
		include 'conexion.php';
		
		$fecha=date("d").'/'.date("m").'/'.date("Y");
		/*
		$xmlInput=
		'{
		"GRP1":{
		"WSIITMREF":"ART1",
		"WSILOT":"LOTE",
		"WSISUBLOT":"001",
		"WSIMFGNUM":"OFFAM210300009"
		}
		}';
		*/
		
		$xmlInput=
		'{
		"GRP1":{
		"WSIITMREF":"'.$codigoarticulo.'",
		"WSILOT":"'.$lote.'",
		"WSISUBLOT":"'.$sublote.'",
		"WSILECDAT":"'.$fecha.'"
		}
		}';
		
		//echo '1';
		$result = $soapClient->run($callContext,"YWSCONSUMO",$xmlInput);
		//echo '2';
		$respuesta= $result->resultXml;
		//echo '3';
		$obj = str_replace('{"GRP1":', "", $respuesta);
		//echo '4';
		$obj = str_replace('}}', "}", $obj);
		//echo '5';
		$obj = json_decode($obj);
		//echo '6';
		
		if ($respuesta==''){
			$mensajedevuelto='ERROR DEL WEBSERVICE';
			$error=1;
			
			header('Location: http://'.$_SERVER['HTTP_HOST'].$_SERVER['REQUEST_URI'].'&red=1');
			exit();
		}else{
			$mensajedevuelto= $obj->{'WERRMSG'};
		}
		//echo '7';
		
	}else{
		echo 'ERROR de Lectura';	
		exit();
	}
}
?>

<!DOCTYPE HTML>
<!--
	Editorial by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html>
	<head>
		<title>Trilla Famesa</title>
		<meta charset="utf-8" />
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<link rel="stylesheet" href="assets/css/main.css" />
	</head>
	<body class="is-preload">

		<!-- Wrapper -->
			<div id="wrapper">

				<!-- Main -->
					<div id="main">
						<div class="inner">

							<!-- Header -->
								<header id="header">
									<a href="index.php" class="logo"><strong>Trilla </strong> Famesa</a>
									<ul class="icons">
										<!--<li><a href="#" class="icon brands fa-twitter"><span class="label">Twitter</span></a></li>
										<li><a href="#" class="icon brands fa-facebook-f"><span class="label">Facebook</span></a></li>
										<li><a href="#" class="icon brands fa-snapchat-ghost"><span class="label">Snapchat</span></a></li>
										<li><a href="#" class="icon brands fa-instagram"><span class="label">Instagram</span></a></li>
										<li><a href="#" class="icon brands fa-medium-m"><span class="label">Medium</span></a></li>-->
									</ul>
								</header>

							
                            
                            
                            
                            	<section>
									<header class="main">
										<h1>4.3.5.8. Consumo</h1>
									</header>

									<!-- Content -->
										<!--<h2 id="content">Sample Content</h2>-->
										<p><form method="get" action="consumo.php">
														<div class="row gtr-uniform">
															<div class="col-6 col-12-xsmall">
																<input type="text" name="consumo" id="consumo" value="<?php 
																if (isset($_GET['consumo'])){
																	echo $consumo;
																}
																?>" placeholder="Código de Envasado" required/>
															</div>
															
															
															<!-- Break -->
															<div class="col-12">
																<ul class="actions">
																	<li><input type="submit" onClick="this.form.submit(); this.disabled=true; this.value='Enviando…'; " value="Aceptar" class=" col-12 primary" /></li>
																	
																</ul>
															</div>
														</div>
													</form>
                                                    
                                                    <?php
													
													if($error==1){
														echo '<h2>SIN RESPUESTA DEL WEBSERVICE</h2>';	
													}
													
													if (isset($_GET['envasado'])){
														echo '<strong>Código de Artículo:</strong> '.$codigoarticulo;
														//echo '<br/><strong>Separador Lote:</strong> '.$separadorlote;
														echo '<br/><strong>Lote:</strong> '.$lote;
														echo '<br/><strong>SubLote:</strong> '.$sublote;
														echo '<br/><strong>Fecha:</strong> '.$fecha.'<br/>';
													}else{
														echo 'EJEMPLO: ART1LOTLOTES1234001';
													}
													
													if (isset($respuesta)){
														echo '<br><h2>'.$mensajedevuelto.'</h2>';
														echo '<div class="alert alert-success" role="alert">DETALLE  DE RESPUESTA:<br/><strong>
														  '.$respuesta.'
														</strong></div>';	
													}
													
													?>
                                                    </p>
										

									<hr class="major" />

									<!-- Elements -->
								</section>

							

						</div>
					</div>

				<!-- Sidebar -->
					<div id="sidebar">
						<div class="inner">

							<img src="images/logo.png" alt="logo trilla" width="200px">

							<!-- Menu -->
								<?php
									include 'menu.php';
								?>

							

						</div>
					</div>

			</div>


<?php
if ($mensajedevuelto=='OK'){
echo '<script type="application/javascript">
	document.getElementById("consumo").value = "";
</script>
';
	
}else{
	/*
	echo '<script type="application/javascript">
	alert(\'no vaciamos caja\');
	document.getElementById("consumo").value = "";
</script>';
*/
	}
?>



		<!-- Scripts -->
			<script src="assets/js/jquery.min.js"></script>
			<script src="assets/js/browser.min.js"></script>
			<script src="assets/js/breakpoints.min.js"></script>
			<script src="assets/js/util.js"></script>
			<script src="assets/js/main.js"></script>

	</body>
</html>