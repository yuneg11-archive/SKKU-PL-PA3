fun list(){
	val fruits = listOf("banana", "avocado", "apple", "kiwifruit")
	fruits.filter{it.startsWith("a")}.sortedBy{it}.map{it.toUpperCase()}.forEach{println(it)}
}

fun main() {
	val items = setOf("apple", "banana", "kiwifruit")
	for (item in items) {
		println(item)
	}
	list()
}

