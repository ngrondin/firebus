function test() {
    var hash = 2547;
    for (var i = 0; i < 100; i++) {
        hash = ((hash<<3)-hash)+i;
        hash = hash & hash; 
    }
    return hash;
}
print(test());