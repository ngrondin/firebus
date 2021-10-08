var a = [1, 2, 3, 3, 5, 6, 2];
print(a.filter((item, index, self) => self.indexOf(item) == index));