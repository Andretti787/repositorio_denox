<?php

if (!empty($_GET)){
	if (isset($_GET['envasado']) && isset($_GET['autocontrol'])){
		$envasado=$_GET['envasado']; //PPPPPPPPLOTLLLLLLLLLSSS
		//$codigoarticulo=substr($envasado, 0, 8); 
		
		$porciones = explode("LOT", $envasado);
		$codigoarticulo = $porciones[0]; // porción1
		
		
		$restante = str_replace($codigoarticulo.'LOT', "", $envasado);
		
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


//echo $sublote;
//exit();


		$autocontrol=$_GET['autocontrol']; 
		
		include 'conexion.php';
		
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
		"WSIMFGNUM":"'.$autocontrol.'"
		}
		}';
		
		
		$result = $soapClient->run($callContext,"YWSASIGM",$xmlInput);
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
<style>
/* The alert message box */
.alert {
  padding: 20px;
  background-color: #D3DEFB; /* Red */
  color: white;
  margin-bottom: 10px;
}

/* The close button */
.closebtn {
  margin-left: 15px;
  color: white;
  font-weight: bold;
  float: right;
  font-size: 22px;
  line-height: 20px;
  cursor: pointer;
  transition: 0.3s;
}

/* When moving the mouse over the close button */
.closebtn:hover {
  color: black;
}
</style>
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
										<h1>4.3.5.4. Asignación</h1>
									</header>
<?php
if (isset($respuesta)){
														echo '<br><div class="alert"><h2><i class="fas fa-info-circle"> </i> '.$mensajedevuelto.'</h2></div>  <button style = "width:100%" class="col-12 primary" onclick="window.location.href=\'/index.php\'">Continuar</button>';

}else{
?>
									<!-- Content -->
										<!--<h2 id="content">Sample Content</h2>-->
										<p><form method="get" action="index.php">
														<div class="row gtr-uniform">
															<div class="col-6 col-12-xsmall">
																<input type="text" name="envasado" id="envasado" value="<?php 
																if (isset($_GET['envasado'])){
																	echo $envasado;
																}
																?>" placeholder="Código de Envasado" required/>
															</div>
															<div class="col-6 col-12-xsmall">
																<input type="text" name="autocontrol" id="autocontrol" value="<?php 
																
																if (isset($_GET['autocontrol'])){
																	echo $autocontrol;
																}
																?>" placeholder="Código de Autocontrol" required/>
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
													
													
													
													if (isset($_GET['envasado']) && isset($_GET['autocontrol'])){
														echo '<strong>Código de Artículo:</strong> '.$codigoarticulo;
														//echo '<br/><strong>Separador Lote:</strong> '.$separadorlote;
														echo '<br/><strong>Lote:</strong> '.$lote;
														echo '<br/><strong>SubLote:</strong> '.$sublote;
														echo '<br/><strong>Autocontrol:</strong> '.$autocontrol.'<br/>';
													}else{
														echo 'EJEMPLO: ART1LOTLOTES1234567001 y OFFAM210300009';
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

							
<?php
}
?>
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
	document.getElementById("autocontrol").value = "";
	document.getElementById("envasado").value = "";
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