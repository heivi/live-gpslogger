<?php
if (isset($_REQUEST['c']) && isset($_REQUEST['lat']) && isset($_REQUEST['lon']) && isset($_REQUEST['acc']) && isset($_REQUEST['time'])) {

	//seurantatunnus.alkuaika_($lon*50000)_($lat*100000).aikamuutos.lonmuutos.latmuutos
  $timemuutos = 1136073600;
  $tofile = $_REQUEST['c'] . "." . ($_REQUEST['time'] - $timemuutos) . "_" . intval($_REQUEST['lon']*50000) . "_" . intval($_REQUEST['lat']*100000) . ".\n";

  if (file_put_contents("data.lst", $tofile, FILE_APPEND)) {
    echo "OK";
  } else {
    echo "Error";
  }
}
?>
