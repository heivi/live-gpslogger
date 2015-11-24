<?php
$timemuutos = 1136073600;

file_put_contents("log.txt", print_r($_REQUEST, TRUE), FILE_APPEND);

if (isset($_REQUEST['c']) && isset($_REQUEST['lat']) && isset($_REQUEST['lon']) && isset($_REQUEST['acc']) && isset($_REQUEST['time'])) {
	
	file_put_contents("log.txt", "single\n\r", FILE_APPEND);

	//seurantatunnus.alkuaika_($lon*50000)_($lat*100000).aikamuutos.lonmuutos.latmuutos
  $tofile = $_REQUEST['c'] . "." . intval($_REQUEST['time'] - $timemuutos) . "_" . intval($_REQUEST['lon']*50000) . "_" . intval($_REQUEST['lat']*100000) . ".\n";

  if (file_put_contents("data.lst", $tofile, FILE_APPEND)) {
    echo "OK";
  } else {
    echo "Error";
  }
} else if (isset($_REQUEST['multiple'])) {
	
	file_put_contents("log.txt", "multiple\n\r", FILE_APPEND);
	
  $tofile = "";
  foreach ($_REQUEST['multiple'] as $point) {
  	 $tofile .= $point['c'] . "." . intval($point['time'] - $timemuutos) . "_" . intval($point['lon']*50000) . "_" . intval($point['lat']*100000) . ".\n";
  }
  
  if (file_put_contents("data.lst", $tofile, FILE_APPEND)) {
    echo "OK";
  } else {
    echo "Error";
  }
  
}
?>
