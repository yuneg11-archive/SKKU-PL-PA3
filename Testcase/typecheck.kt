fun getStringLength(obj: Any): Int? {
	if (obj is String && obj.length > 0) {
		return obj.length
	}
	return null
}

fun main() {
	fun printLength(obj: Any) {
		println("'$obj' string length is ${getStringLength(obj)}")
	}
	printLength("Incomprehensibilities")
	printLength("")
	printLength(1000)
}
