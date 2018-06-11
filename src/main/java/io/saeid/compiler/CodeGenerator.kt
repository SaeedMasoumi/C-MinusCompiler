package io.saeid.compiler


/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */
typealias Address = Int

class CodeGenerator(private val reduces: List<Reduce>) {
    companion object {
        const val ARRAY = "array"
        const val INT = "array"
    }

    private val symbolTable = SymbolTable()

    private var lastAddress = 500
    private var dataAddress = 100
    private var ss = mutableListOf<String>()
    private val pb: MutableMap<Address, String> by lazy {
        val a = mutableMapOf<Address, String>()
        for (i in 0 until 200)
            a[i] = ""
        return@lazy a
    }
    private var i = 0
    private var currentFunction = ""
    private var isArray = false
    private var argsIsArray = false
    private var expIsArray = false

    private fun nextTemp(): Int {
        lastAddress += 4
        return lastAddress - 4
    }

    private fun nextDataAddress(): Int {
        dataAddress += 4
        return dataAddress - 4
    }

    fun generator() {
        reduces.forEach { reduce ->
            println(reduce)
            when (reduce.rule.left) {
                "FID" -> fid(reduce)
                "INCR" -> increase(reduce)
                "DECR" -> decrease(reduce)
                "PARAMID" -> paramid(reduce)
                "FUN_IN" -> funin(reduce)
                "PID" -> pid(reduce)
//                "SIZE_IS" -> sizeis(reduce)
                "ASSIGN" -> assign(reduce)
                "ADD" -> add(reduce)
                "SUB" -> sub(reduce)
                "MULT" -> mult(reduce)
                "EQ" -> eq(reduce)
                "LT" -> lt(reduce)
//                "SID" -> sid(reduce)
//                "SSID" -> ssid(reduce)
//                "LABEL" -> label(reduce)
            }
        }
        symbolTable.print()
        pb.forEach { t, u ->
            if (u.isNotEmpty())
                println(u)
        }
        println("--")
        ss.forEach {
            println(it)
        }
    }


    private fun decrease(reduce: Reduce) {
        symbolTable.decrease()
    }

    private fun pid(reduce: Reduce) {
        val varName = reduce.prev.name
    }

    private fun funin(reduce: Reduce) {
        //no-op
    }

    private fun add(reduce: Reduce) {
        if (ss.size > 1) {
            val nextTemp = nextTemp()
            pb[i] = "(ADD, ${ss[ss.size - 1]}, ${ss[ss.size - 2]}, $nextTemp"
            ss.pop2()
            ss.add(nextTemp.toString())
            i++
        }
    }

    private fun sub(reduce: Reduce) {
        if (ss.size > 1) {
            val nextTemp = nextTemp()
            pb[i] = "(SUB, ${ss[ss.size - 1]}, ${ss[ss.size - 2]}, $nextTemp"
            ss.pop2()
            ss.add(nextTemp.toString())
            i++
        }
    }

    private fun mult(reduce: Reduce) {
        if (ss.size > 1) {
            val nextTemp = nextTemp()
            pb[i] = "(MULT, ${ss[ss.size - 1]}, ${ss[ss.size - 2]}, $nextTemp"
            ss.pop2()
            ss.add(nextTemp.toString())
            i++
        }
    }

    private fun eq(reduce: Reduce) {
        if (ss.size > 1) {
            val nextTemp = nextTemp()
            pb[i] = "(EQ, ${ss[ss.size - 1]}, ${ss[ss.size - 2]}, $nextTemp"
            ss.pop2()
            ss.add(nextTemp.toString())
            i++
        }
    }

    private fun lt(reduce: Reduce) {
        if (ss.size > 1) {
            val nextTemp = nextTemp()
            pb[i] = "(LT, ${ss[ss.size - 1]}, ${ss[ss.size - 2]}, $nextTemp"
            ss.pop2()
            ss.add(nextTemp.toString())
            i++
        }
    }

    private fun assign(reduce: Reduce) {
        if (ss.size > 1) {
            pb[i] = "(ASSIGN, ${ss[ss.size - 1]}, ${ss[ss.size - 2]})"
            ss.pop2()
            i++
        }
    }

    private fun MutableList<String>.pop2() {
        this.removeAt(this.size - 1)
        this.removeAt(this.size - 2)
    }

    private fun label(reduce: Reduce) {
    }

    private fun sid(reduce: Reduce) {
    }

    private fun sizeis(reduce: Reduce) {
    }

    private fun paramid(reduce: Reduce) {
        val token = reduce.prev
        val type = reduce.old.name
        val name = reduce.prev.name
        val isArray = reduce.cur.name == "["
        val addr = nextTemp()
        val functionSymbolItem = symbolTable.get(currentFunction)
        if (isArray)
            functionSymbolItem.args.add("array")
        else
            functionSymbolItem.args.add("int")
        symbolTable.insert(token, Item(
                address = addr, isVariable = true, isInt = !isArray, isReference = true
        ))

    }

    private fun increase(reduce: Reduce) {
        symbolTable.increase()
    }

    private fun fid(reduce: Reduce) {
        val token = reduce.prev
        val funcName = reduce.prev.name
        val funcType = reduce.old.name
        currentFunction = funcName
        val add1 = nextTemp()
        val add2 = nextTemp()
        val isVoid = funcType != "int"
        symbolTable.insert(token,
                Item(isVoid = isVoid, isInt = !isVoid, callbackAddress = add1, returnAddress = add2,
                        inputAddress = add2 + 4))
        if (funcName == "main")
            pb[0] = "(JP, $i)"

    }
}