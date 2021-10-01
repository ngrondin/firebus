var dt = new Date();
var r = dt.setDate(32);
print(dt);

function now() {
	var dt = new Date();
	return dt;
}

function newDate(dur) {
	return new Date(dur); 
}

function dateAddDuration(dt, dur) {
	if(dt != null && dur != null) {
		return new Date(parseInt(dt.getTime()) + parseInt(dur));
	} else {
		return null;
	}
}

function dateDiff(dt1, dt2) {
	if(dt1 != null && dt2 != null) {
		return dt2.getTime() - dt1.getTime();
	} else {
		return 0;
	}
}

function updateDate(obj, attr, dt) {
	if(dt != null) {
		if(obj[attr] == null || (obj[attr] != null && obj[attr].getTime() != dt.getTime()))
			obj[attr] = dt;
	} else {
		obj[attr] = null;
	}
}

function midnightOf(dt) {
	var mo = new Date(parseInt(dt.getTime()));
	mo.setHours(0);
	mo.setMinutes(0);
	mo.setSeconds(0);
	mo.setMilliseconds(0);
	return mo;
}

function smallestDate(d1, d2) {
	if(d1 == null && d2 == null)
		return null;
	else if(d1 != null && d2 == null)
		return d1;
	else if(d1 == null && d2 != null)
		return d2;
	else if(d1.getTime() < d2.getTime())
		return d1;
	else 
		return d2;
}

function biggestDate(d1, d2) {
	if(d1 == null && d2 == null)
		return null;
	else if(d1 != null && d2 == null)
		return d1;
	else if(d1 == null && d2 != null)
		return d2;
	else if(d1.getTime() > d2.getTime())
		return d1;
	else 
		return d2;
}

function getTimezoneDiff(dt, zoneId) {
	var diff = 0;
	if(zoneId != null && zoneId != '') {
		var localOffset = dt.getTimezoneOffset() * 60000;
		var targetOffset = rbutils.getTimezoneOffset(zoneId);
		diff = targetOffset - localOffset;
	}
	return diff;
}

function getDateAtTimezone(dt, zoneId) {
	var diff = getTimezoneDiff(dt, zoneId);
	var dtOut = new Date(dt.getTime() - diff);
	return dtOut;
}

function getMidnightAtTimezone(dt, zoneId) {
	var diff = getTimezoneDiff(dt, zoneId);
	var dtMid = midnightOf(new Date(dt.getTime() - diff));
	var dtOut = new Date(dtMid.getTime() + diff);
	return dtOut;	
}

function getDayOfWeekAtTimezone(dt, zoneId) {
	return getDateAtTimezone(dt, zoneId).getDay();
}

function getDayOfMonthAtTimezone(dt, zoneId) {
	return getDateAtTimezone(dt, zoneId).getDate();
}

function getTimeLabelAtTimezone(dt, zoneId) {
	if(dt != null) {
		var ndt = getDateAtTimezone(dt, zoneId);
		return ndt.getHours() + ':' + ('0' + ndt.getMinutes()).slice(-2);
	} else { 
		return '';
	}
}

function getDateLabelAtTimezone(dt, zoneId) {
	if(dt != null) {
		var ndt = getDateAtTimezone(dt, zoneId);
		return ndt.getFullYear() + '-' + ('0' + (ndt.getMonth() + 1)).slice(-2) + '-' + ('0' + ndt.getDate()).slice(-2);
	} else { 
		return '';
	}
}

function getIsoDateTimeAtTimezone(dt, zoneId) {
	if(dt != null) {
		var ndt = getDateAtTimezone(dt, zoneId);
		return ndt.getFullYear() + '-' + ('0' + (ndt.getMonth() + 1)).slice(-2) + '-' + ('0' + ndt.getDate()).slice(-2) + 'T' + ('0' + ndt.getHours()).slice(-2) + ':' + ('0' + ndt.getMinutes()).slice(-2) + ':' + ('0' + ndt.getSeconds()).slice(-2);
	} else { 
		return '';
	}
}


