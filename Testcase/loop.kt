fun main(){
	for (i in 1..10){
		var index = 0
		var r = 11-i
		for(j in r downTo 0){
			print(" ")
		}
		while(index < i){
			print("*")
			index++
		}
		println()
	}
}
