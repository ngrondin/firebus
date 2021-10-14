var t = new Time('T05:00:00[Australia/Perth]');
print(t);
var now = new Date();
print(now);
var dt = t.atDate(now);
print(dt);
print(dt.getHours());