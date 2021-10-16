var geo = {
	geometry:{
		coords:{
			latitude: -33.234,
			longitude: 151.337
		}
	}
}
var geo2 = {
	geometry:{
		coords:{
			latitude: -33.434,
			longitude: 152.337
		}
	}
}

var lat1 = geo.geometry.coords.latitude;
var lon1 = geo.geometry.coords.longitude;
var lat2 = geo2.geometry.coords.latitude;
var lon2 = geo2.geometry.coords.longitude;
var r = 6371e3;
var p1 = lat1 * Math.PI/180; 
var p2 = lat2 * Math.PI/180;
var dp = (lat2-lat1) * Math.PI/180;
var dl = (lon2-lon1) * Math.PI/180;
var a = Math.sin(dp/2) * Math.sin(dp/2) +
          Math.cos(p1) * Math.cos(p2) *
          Math.sin(dl/2) * Math.sin(dl/2);
var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
dist = r * c;    
print(dist);