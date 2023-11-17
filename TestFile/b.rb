x = 'hi'
=begin
comparison test case
=end
puts 'si'<="si", 'si' != "si","s">"b","s"<"b"
puts 2==2.0,2.0==2,2==2.1
print 2 ** -1
def fib(n)
  if n==0
    return 1
  end
  if n==1
    return 1
  end
  if n!=1 or n!=0
    return fib(n-1)+fib(n-2)
  end
end

puts fib(1)
puts fib(2)
puts fib(3)
puts fib(4)
puts fib(5)
puts fib(6)

def a
  return 1
end

puts a()
=begin
true
false
true
false
true
true
false
1/21
2
3
5
8
13
1
=end
