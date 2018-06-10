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

    private var errorNameSpace: String = ""
    private var lastAddress = 200
    private var ss = mutableListOf<String>()
    private val pb: MutableMap<Address, String> by lazy {
        val a = mutableMapOf<Address, String>()
        for (i in 0 until 200)
            a[i] = ""
        return@lazy a
    }
    private var i = 0
    private var isVoid = true
    private var currentFunc = ""
    private var isArray = false
    private var argsIsArray = false
    private var expIsArray = false

    fun getTemp(): Int {
        lastAddress += 4
        return lastAddress - 4
    }

    fun generator() {
        reduces.forEach { reduce ->
            println(reduce.rule.left)
            when (reduce.rule.left) {
                "FID" -> fid(reduce)
                "INCR" -> increase(reduce)
                "FUN_IN" -> funin(reduce)
                "PID" -> pid(reduce)
//                "PARAMID" -> paramid(reduce)
//                "SIZE_IS" -> sizeis(reduce)
//                "ASSIGN" -> assign(reduce)
                "SID" -> sid(reduce)
//                "SSID" -> ssid(reduce)
                "LABEL" -> label(reduce)
            }
        }
        pb.forEach { t, u ->
            if (u.isNotEmpty())
                println(u)
        }
        println("--")
        ss.forEach {
            println(it)
        }
    }

    private fun pid(reduce: Reduce) {
        val token = reduce.prev
        val attrs = symbolTable.lookup(token.name)
        isArray = false
        var addr = 0
//        if (attrs == null) {
//            addr = 0
//        } else if (!attrs.isVar) {
//            Logger.error("$token is not a variable")
//            addr = 0
//        } else {
//            addr = attrs.address
//            if (!attrs.isInt)
//                isArray = true
//            if (!argsIsArray) {
//                Logger.error("$token is an array")
//            }
//        }
//        expIsArray = isArray && !expIsArray
        ss.add(addr.toString())
    }

    private fun funin(reduce: Reduce) {
        errorNameSpace = currentFunc
    }

    private fun ssid(reduce: Reduce) {
        val prev = reduce.prev
        symbolTable.insert(prev, SymbolScope(address = getTemp(), isVar = true, isInt = false))
        ss.add(prev.name)
    }

    private fun assign(reduce: Reduce) {
        pb[i] = "(ASSIGN, ${ss.last()}, ${ss[ss.size - 2]})"
        ss.removeAt(ss.size - 1)
        ss.removeAt(ss.size - 1)
        i += 1
    }

    private fun label(reduce: Reduce) {
        ss.add(i.toString())
    }

    private fun sid(reduce: Reduce) {
        symbolTable.insert(reduce.prev,
                SymbolScope(address = getTemp(), isVar = true, isInt = true))
    }

    private fun sizeis(reduce: Reduce) {
        val cur = reduce.cur
        var size = cur.name.toInt()
        if (size <= 0) {
            Logger.error("length of arrays should be a positive integer")
            size = 1
        }
        symbolTable.update(ss.last(), size)
        ss.removeAt(ss.size - 1)
        for (i in 0 until size - 1)
            getTemp()
    }

    private fun paramid(reduce: Reduce) {
        val token = reduce.prev
        val x = reduce.cur
        isArray = false
        if (x.name == "[")
            isArray = true
        val addr = getTemp()
        symbolTable.insert(token, SymbolScope(address = addr, isVar = true, isInt = !isArray,
                isReference = true))
        val arguments = symbolTable.lookup(currentFunc)?.arguments
        if (isArray)
            arguments?.add(ARRAY)
        else
            arguments?.add(INT)
        arguments?.let {
            symbolTable.scopes[0].update(token = currentFunc, arguments = arguments)
        }
    }

    private fun increase(reduce: Reduce) {
        symbolTable.increase()
    }

    private fun fid(reduce: Reduce) {
        val (_, _, token, old) = reduce
        val add1 = getTemp()
        val add2 = getTemp()
        isVoid = old.typeToTableName() != "int"
        currentFunc = token.name
        symbolTable.insert(token,
                SymbolScope(address = i, isVar = false, isInt = true, callbackAddress = add1,
                        returnAddress = add2, inputAddress = add2 + 4,
                        arguments = mutableListOf(), isVoid = isVoid, isReference = true))
        if (currentFunc == "main")
            pb[0] = "(JP, $i)"
    }
}