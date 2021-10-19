var available = true;
var lasttrack = 0;
print(lasttrack != null && ((new Date()).getTime() - lasttrack.getTime()) < 900000 ? 'good' : 'nocontact');
