<?
include('conn.php');
function rand_color()
{
//$rand = dechex(rand(0x000000, 0xFFFFFF));
$rand='rgb('.rand(1,255).','.rand(1,255).','.rand(1,255).')';
//$randarray=array_push($rand);
return $rand;
}
$query = mysqli_query($con,"SELECT count(*) as count FROM `match` WHERE date_played='".date("Y-m-d")."';");
$row = mysqli_fetch_array($query);
if($row['count']!=0)
{
$query = mysqli_query($con,"SELECT * FROM `match` WHERE date_played='".date("Y-m-d")."';");
$i=0;
					while($row = mysqli_fetch_array($query))
					{
					$wordcount_values='';
					$legends_values='';
					$color_values='';
					$query1 = mysqli_query($con,"SELECT * from `word_tracker` where match_id=".$row['match_id']." order by count desc LIMIT 0 , 10;");
					while($row1 = mysqli_fetch_array($query1))
					{ $wordcount_values.= "['".$row1['word']."', ".$row1['count']."],";					  
					  $rand=rand_color();
					  $color_values.="'".$rand."',";					  
					  $legends_values.='<span style="background-color:'.$rand.'"></span>'.$row1['word'].'<br/>';			  
					}
					$wordcount[$i]=$wordcount_values;
					$legends[$i]=$legends_values;
					$color[$i]=$color_values;
					$teams[$i]=explode(',',$row['teams']);
					$venue[$i]=$row['venue'];
					$yahoo_match_id[$i]=$row['yahoo_match_id'];
					$match_id[$i]=$row['match_id'];					
					$i++;}
?><!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<meta name="description" content="">
		<meta name="author" content="">
		<link rel="icon" href="favicon.ico">

		<title>Hash Scores: England vs India</title>

		<!-- Bootstrap core CSS -->
		<link href="dist/css/bootstrap.min.css" rel="stylesheet">

		<!-- Custom styles for this template -->
		<link href="specific/offcanvas.css" rel="stylesheet">
		<link href="specific/hashscores.css" rel="stylesheet">
		<link href='http://fonts.googleapis.com/css?family=Roboto+Condensed' rel='stylesheet' type='text/css'>
		
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
		
		<script type="text/javascript">
			google.load("visualization", "1", {packages:["corechart"]});
		</script>
		<script>
setInterval(function () {location.reload();}, 60000);
function loadXMLDoc()
{
var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
  xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
  xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
xmlhttp.onreadystatechange=function()
  {
  if (xmlhttp.readyState==4 && xmlhttp.status==200)
    {
    document.getElementById("ajax_content").innerHTML=xmlhttp.responseText;
    }
  }
xmlhttp.open("GET","ajax.php",true);
xmlhttp.send();
}
</script>
		<!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
		<!--[if lt IE 9]><script src="assets/js/ie8-responsive-file-warning.js"></script><![endif]-->
		<script src="assets/js/ie-emulation-modes-warning.js"></script>

		<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
		<script src="assets/js/ie10-viewport-bug-workaround.js"></script>

		<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
		<!--[if lt IE 9]>
		  <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
		  <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
		<![endif]-->
	</head>

	<body>
    <div class="navbar navbar-static-top navbar-default" role="navigation">
		<div class="container">
			<div class="navbar-header">
			  <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			  </button>
			  <a class="navbar-brand" href="#">
				#Scores
			  </a>
			</div>
			<div class="navbar-collapse collapse">
			  <ul class="nav navbar-nav">
			  </ul>
			  <ul class="nav navbar-nav navbar-right">
				<li><a href="#">Home</a></li>
				<li class="#"><a href="./">About Us</a></li>
				<li class="#"><a href="./">Archive</a></li>
				<li><a href="#">Terms</a></li>
				<li><a href="#">Blog</a></li>
				<li><a href="#">Contact Us</a></li>
			  </ul>
			</div><!--/.nav-collapse -->
		</div><!-- /.container -->
    </div><!-- /.navbar -->

    <div class="container">
		<div class="row row-offcanvas row-offcanvas-right">
			<div class="col-xs-12 col-sm-8">
				<p class="pull-right visible-xs">
				<button type="button" class="btn btn-primary btn-xs" data-toggle="offcanvas">Other Matches</button>
				</p>
				<div class="jumbotron text-center">
					<h2><?for($j=0;$j<$i;$j++)
							{echo $teams[$j][0].' vs '.$teams[$j][1];?></h2>
							<p><?echo $venue[$j];?></p>				
			  		<h2><?}?>
						<a href="#">
							<span class="glyphicon glyphicon-refresh" onclick="location.reload();"></span>
						</a>
					</h2>
				</div>					
<?for($j=0;$j<$i;$j++)
{?>				
				<div class="row">
					<script type="text/javascript">
						google.setOnLoadCallback(drawChart_2nd_34_2);
						function drawChart_2nd_34_2() {
							var data = google.visualization.arrayToDataTable([
								['Keywords', 'Percentage'],
								<?echo $wordcount[$j];?>
							]);

							var options = {
								legend: 'none',
								pieSliceText: 'none',
								tooltip: { trigger: 'selection', text: 'percentage' },
								backgroundColor: 'none',
								colors:[<?echo $color[$j];?>]				
							};

							var chart = new google.visualization.PieChart(document.getElementById('piechart_<?echo $match_id[$j]?>'));
							chart.draw(data, options);
						}
					</script>				
					<div class="col-6 col-sm-6 col-lg-6">
						<h4><?echo $teams[$j][0];
						$geturl=file_get_contents("https://query.yahooapis.com/v1/public/yql?q=select%20past_ings%20from%20cricket.scorecard%20where%20match_id%3D".$yahoo_match_id[$j]."&format=json&diagnostics=true&env=store%3A%2F%2F0TxIGQMQbObzvU4Apia0V0&callback=");
						$data=json_decode($geturl ,true);?>: <strong><?echo $data['query']['results']['Scorecard'][1]['past_ings']['s']['a']['r'].'/'.$data['query']['results']['Scorecard'][1]['past_ings']['s']['a']['w'].'('.$data['query']['results']['Scorecard'][1]['past_ings']['s']['a']['o'].')';?></strong></h4>
						<!--<p>R Jadeja* 9(12) . MS Dhoni 323(100)</p>
						<br/>-->
						<h4><?echo $teams[$j][1]; ?>: <strong><?echo $data['query']['results']['Scorecard'][0]['past_ings']['s']['a']['r'].'/'.$data['query']['results']['Scorecard'][0]['past_ings']['s']['a']['w'].'('.$data['query']['results']['Scorecard'][0]['past_ings']['s']['a']['o'].')';?></strong></h4> 
						<!--<p>L Malinga - 6.5 Overs / 56 Runs / 2 Wickets</p>
						<br/>-->
					</div><!--/span-->
					<div class="col-6 col-sm-6 col-lg-6">
						<h4>Sentiment (in %)</h4>
						<div class="canvas-holder width_100pc">
							<div id="piechart_<?echo $match_id[$j];?>" style="width: 200px;"></div>
						</div>
						<div class="legend">
							<!--<span style="background-color:#F7464A"></span>Anushka - Best - Captain - Dhoni - Sharma<br/>
							<span style="background-color:#1975ff"></span>Anushka - Friend - Kohli - Sharma<br/>
							<span style="background-color:#FDB45C"></span>Idiots - Pakistan<br/>
							<span style="background-color:#53bc73"></span>Bad - MSD<br/>
							<span style="background-color:#df51c6"></span>Captain - Raina<br/>-->
							<?echo $legends[$j];?>
						</div>
					</div><!--/span-->
				</div><hr/><?}?><!--/row-->				
				<div class="row">
					<div class="col-12 col-sm-12 col-lg-12 text-center">
						<span class="muted add_color_gray">
							You’ve reached the end!
						</span>
					</div>
				</div><!--/row-->			
			</div><!--/span-->
			<div class="col-xs-6 col-sm-4 sidebar-offcanvas" id="sidebar" role="navigation">
				<div class="list-group">
				<?for($j=0;$j<$i;$j++)
							{?>
					<a href="#" class="list-group-item active"><?echo $teams[$j][0].' vs '.$teams[$j][1];?></a><?}?>
					<?				 
					$query = mysqli_query($con,"SELECT * FROM `match` WHERE date_played='".date("Y-m-d",strtotime($Date. ' + 1 days'))."';");
					while($row = mysqli_fetch_array($query)) 
					{
					$teams_tomorrow=explode(',',$row['teams']);
					?>
					<a href="#" class="list-group-item"><?echo $teams_tomorrow[0].' vs '.$teams_tomorrow[1];?> - Tomorrow</a>
			         <?}?>  
				</div>
				<div class="row">
					<div class="col-xs-12 text-center">
						<img class="width_100pc" src="img/ad_square.jpg">
					</div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-12 text-center">
						<img class="width_100pc" src="img/ad_square.jpg">
					</div>
				</div>		  
			</div><!--/span-->
		</div><!--/row-->
		<hr>
		<footer>
			<p>&copy; Hash Scores 2014</p>
		</footer>
    </div><!--/.container-->

    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script src="dist/js/bootstrap.min.js"></script>
    <script src="specific/offcanvas.js"></script>
	<script src="specific/Vendor/Chart.js"></script>
  </body>
</html>
<?}?>