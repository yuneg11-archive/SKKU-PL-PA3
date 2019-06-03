fun maxOf1(a: Int, b: Int): Int {
	if (a > b) {
		return a
	} else {
		return b
	}
}

fun maxOf2(a: Int, b: Int) = if (a > b) a else b

fun main() {
	println("max of 0 and 42 is ${maxOf1(0, 42)}")
	println("max of 12 and 22 is ${maxOf2(12, 22)}")
}
