var a = [null, 0, 1, 2, "0", "1", "2", "", "string", true, false];
for(var i = 0; i < a.length; i++)
	for(var j = 0; j < a.length; j++)
		print(a[i] + " == " + a[j] + " -> " + (a[i] == a[j]));
