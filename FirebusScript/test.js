var dt = new Date();
var list = [
{dt: dt, num: 3, str:"allo"},
{dt: dt, num: 4, str:"toi"}
];
print(list.find(item => item.dt.getTime() == dt.getTime() && item.str == "allo"));