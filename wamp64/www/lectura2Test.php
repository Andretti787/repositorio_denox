<?php

if (!empty($_GET)){
	if (isset($_GET['etiqueta'])){
		$etiqueta=$_GET['etiqueta']; //PPPPPPPPLOTLLLLLLLLLSSS
		//$codigoarticulo=substr($envasado, 0, 8); 
		
		$porciones = explode("Q", $etiqueta);
		$nof = $porciones[0]; // porción1
		
		
		$restante = str_replace($nof.'Q', "", $etiqueta);
		
		$restante = explode("E", $restante);
		$cantidad = $restante[0];
		$numetiqueta = $restante[1];
		include 'conexionTest.php';
		
		
		
		/*
		$xmlInput=
		'{
		"GRP1":{
		"WSIITMREF":"'.$codigoarticulo.'",
		"WSILOT":"'.$lote.'",
		"WSISUBLOT":"'.$sublote.'",
		"WSIMFGNUM":"'.$autocontrol.'"
		}
		}';
		*/
		$fecha=date("d").'/'.date("m").'/'.date("Y");
		$hora=date("h:i:s");
		$xmlInput=
		'{
		"GRP1":{
		"WSIOF":"'.$nof.'",
		"WSINETIQ":"'.$numetiqueta.'",
		"WSILECDAT":"'.$fecha.'",
		"WSILECHOR":"'.$hora.'"
		}
		}';
		
		/*
		$xmlInput='
		{
		"GRP1":{
		"WSIOF":"OFFAM210300009",
		"WSINETIQ":"1",
		"WSILECDAT":"16/06/2021",
		"WSILECHOR":"15:10:10"
		}
		}';
		*/

		$result = $soapClient->run($callContext,"YWSLECET1",$xmlInput);
		$respuesta= $result->resultXml;

$obj = str_replace('{"GRP1":', "", $respuesta);
$obj = str_replace('}}', "}", $obj);

		$obj = json_decode($obj);
		$mensajedevuelto= $obj->{'WERRMSG'};
		
	}else{
		
		if (isset($_GET['pico'])){
			//echo 'llamamos al segundo WS con los 3 valores get';
			$pico=$_GET['pico'];
			$nof=$_GET['nof'];
			$numetiqueta=$_GET['numetiqueta'];
			
			include 'conexionTest.php';
		
		
		
		/*
		$xmlInput=
		'{
		"GRP1":{
		"WSIITMREF":"'.$codigoarticulo.'",
		"WSILOT":"'.$lote.'",
		"WSISUBLOT":"'.$sublote.'",
		"WSIMFGNUM":"'.$autocontrol.'"
		}
		}';
		*/
		//$fecha=date("d").'/'.date("m").'/'.date("Y");
		//$hora=date("h:i:s");
		$xmlInput=
		'{
		"GRP1":{
		"WSIOF":"'.$nof.'",
		"WSINETIQ":"'.$numetiqueta.'",
		"WSIQTYPIC":"'.$pico.'"
		}
		}';
		
		/*
		$xmlInput='
		{
		"GRP1":{
		"WSIOF":"OFFAM210300009",
		"WSINETIQ":"1",
		"WSILECDAT":"16/06/2021",
		"WSILECHOR":"15:10:10"
		}
		}';
		*/
		//echo $xmlInput;
//exit();
		$result = $soapClient->run($callContext,"YWSLECET2",$xmlInput);
		$respuesta= $result->resultXml;

		$obj = str_replace('{"GRP1":', "", $respuesta);
		$obj = str_replace('}}', "}", $obj);

		$obj = json_decode($obj);
		$mensajedevuelto= $obj->{'WERRMSG'};
			
			
		}else{
			echo 'ERROR de Lectura';	
			exit();
		}
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
									<a href="indexTest.php" class="logo"><strong>Trilla </strong> Famesa</a>
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
										<h1>Introduzca un PICO TEST</h1>
									</header>

									<!-- Content -->
										<!--<h2 id="content">Sample Content</h2>-->
										<p><form method="get" action="lectura2Test.php">
														<div class="row gtr-uniform">
															<div class="col-6 col-12-xsmall">
																<input type="text" name="pico" id="pico" value="" placeholder="Cantidad de Pico" required/>
															</div>
                                                            
                                                            
															
															
															<!-- Break -->
															<div class="col-12">
																<ul class="actions">
																	<li><input type="submit" value="Aceptar" class=" col-12 primary" <?php if (isset($pico)){
																		echo 'disabled';
																		}
                                                                    
																	?>/></li>
																	
																</ul>
                                                                
                                                                <div class="col-6 col-12-xsmall">
																<input style="visibility:hidden" type="text" name="numetiqueta" id="numetiqueta" value="<?php 
																if (isset($numetiqueta)){
																	echo $numetiqueta;
																}
																?>" placeholder="Num Etiqueta" required/>
															</div>
                                                            
                                                            <div class="col-6 col-12-xsmall">
																<input style="visibility:hidden" onClick="this.form.submit(); this.disabled=true; this.value='Enviando…'; " type="text" name="nof" id="nof" value="<?php 
																if (isset($nof)){
																	echo $nof;
																}
																?>" placeholder="OF" required/>
															</div>
															</div>
														</div>
													</form>
                                                    
                                                    <?php
													
													
													/*
													if (isset($_GET['etiqueta'])){
														echo '<strong>Código de OF:</strong> '.$nof;
														//echo '<br/><strong>Separador Lote:</strong> '.$separadorlote;
														echo '<br/><strong>Cantidad:</strong> '.$cantidad;
														echo '<br/><strong>Numetiqueta:</strong> '.$numetiqueta;
														
													}else{
														echo 'EJEMPLO: OFFAM210300009Q3333_1111';
													}
													
													
													
													*/
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
									include 'menuTest.php';
								?>

							

						</div>
					</div>

			</div>
<?php
if ($mensajedevuelto=='OK'){
echo '<script type="application/javascript">
	document.getElementById("pico").value = "";
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