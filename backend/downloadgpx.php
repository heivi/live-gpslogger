<?php
if (isset($_POST['gpxdata']) && isset($_POST['gpxfilename'])) {
  header("Content-Type: text/html");
  //header("Content-Disposition: attachment; filename=\"".$_POST['gpxfilename'].".gpx\"");
  //echo $_POST['gpxdata'];
  $filename = "";
  if (!file_exists("gpx/".$_POST['gpxfilename'].".gpx")) {
    $filename = $_POST['gpxfilename'].".gpx";
    file_put_contents("gpx/".$filename, $_POST['gpxdata']);
  } else {
    $filename = date("U").$_POST['gpxfilename'].".gpx";
    file_put_contents("gpx/".$filename, $_POST['gpxdata']);
  }

  echo '<html><body><a href="gpx/'.$filename.'" target="_blank">http://gps.heikin.tk/logger/gpx/'.$filename.'</a></body></html>';
} else {
  echo "fail";
}
?>
