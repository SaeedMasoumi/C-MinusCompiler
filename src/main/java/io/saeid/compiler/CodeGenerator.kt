package io.saeid.compiler


/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */
typealias Address = Int

class CodeGenerator(private val reduces: List<Reduce>) {

    private val symbolTable = SymbolTable()

    private var lastAddress = 500
    private var dataAddress = 100
    private var ss = mutableListOf<String>()
    private val pb: MutableMap<Address, String> by lazy {
        val a = mutableMapOf<Address, String>()
        for (i in 0 until 100)
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
            println(reduce.rule.left)
            when (reduce.rule.left) {
                "FID" -> fid(reduce)
                "INCR" -> increase(reduce)
                "DECR" -> decrease(reduce)
                "PARAMID" -> paramid(reduce)
                "FUN_IN" -> funin(reduce)
                "PID" -> pid(reduce)
                "PREVIDAR" -> prevIdAr(reduce)
                "SIZE_IS" -> sizeis(reduce)
                "ASSIGN" -> assign(reduce)
                "ADD" -> add(reduce)
                "SUB" -> sub(reduce)
                "MULT" -> mult(reduce)
                "EQ" -> eq(reduce)
                "LT" -> lt(reduce)
                "SID" -> sid(reduce)
                "SIZEARRID" -> sizeArrID(reduce)
                "FUN_OUT" -> funout(reduce)
                "SAVE" -> save(reduce)
                "JP" -> jp(reduce)
                "JPF_SAVE" -> jpf_save(reduce)
                "LOOP" -> loop(reduce)
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

    private fun loop(reduce: Reduce) {
        symbolTable.get(currentFunction)?.let {
            if (it.isVoid) {
                Logger.error("Function $currentFunction is void but it return ${reduce.prev.name}")
            }
        }
    }

    private fun jp(reduce: Reduce) {
        try {
            pb[ss.last().toInt()] = "(JPF, ${ss[ss.size - 2]}, $i)"
            ss.pop2()
        } catch (e: Exception) {
            e.message?.let { Logger.log(it) }
        }
    }

    private fun jpf_save(reduce: Reduce) {
        try {
            pb[ss.last().toInt()] = "(JPF, ${ss[ss.size - 2]}, ${i + 1})"
            ss.pop2()
            ss.add(i.toString())
            i += 1
        } catch (e: Exception) {
            e.message?.let { Logger.log(it) }
        }
    }

    private fun save(reduce: Reduce) {
        ss.add(i.toString())
        i++
    }

    private fun funout(reduce: Reduce) {
        val callbackaddr = symbolTable.get(currentFunction)?.callbackAddress
        pb[i] = "(JP, @$callbackaddr)"
    }

    private fun decrease(reduce: Reduce) {
        symbolTable.decrease()
    }

    private fun pid(reduce: Reduce) {
        val varName = reduce.prev.name
        val item = symbolTable.get(varName)
        var isError = false
        if (item == null) {
            Logger.error("$varName is not defined")
            isError = true
        } else if (!item.isVariable) {
            isError = true
            Logger.error("$varName is not variable")
        } else isArray = !item.isInt
        val address = if (isError) 0 else item!!.address
        ss.add(address.toString())
    }

    private fun prevIdAr(reduce: Reduce) {
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
        this.removeAt(this.size - 1)
    }

    private fun label(reduce: Reduce) {
    }

    private fun sid(reduce: Reduce) {
        val nextAddr = nextDataAddress()
        val token = reduce.prev
        val item = symbolTable.get(token.name)
        if (item != null) {
            Logger.error("${token.name} is already defined")
        }
        symbolTable.insert(token, Item(isVariable = true, isInt = true, address = nextAddr))
        ss.add(token.name)
    }

    private fun sizeArrID(reduce: Reduce) {
        val nextAddr = nextDataAddress()
        val token = reduce.prev
        val item = symbolTable.get(token.name)
        if (item != null) {
            Logger.error("${token.name} is already defined")
        }
        val args = mutableListOf<String>()
        if (reduce.cur.name == "[")
            args.add("array")
        symbolTable.insert(token, Item(isVariable = true, isInt = true, address = nextAddr,args = args))
        ss.add(token.name)
    }

    private fun sizeis(reduce: Reduce) {
        val token = reduce.cur
        val arraySize = token.name.toInt()
        symbolTable.getLast().lookup(ss.last())?.size = arraySize
        if (arraySize < 0) {
            Logger.error("Array length can't be negative [$arraySize]")
        } else {
            for (i in 0 until arraySize - 1)
                nextDataAddress()
        }
    }

    private fun paramid(reduce: Reduce) {
        val token = reduce.prev
        val type = reduce.old.name
        val name = reduce.prev.name
        val isArray = reduce.cur.name == "["
        val addr = nextTemp()
        val functionSymbolItem = symbolTable.get(currentFunction)
        if (isArray)
            functionSymbolItem?.args?.add("array")
        else
            functionSymbolItem?.args?.add("int")
        symbolTable.insert(token, Item(
                address = addr, isVariable = true, isInt = !isArray))
    }

    private fun increase(reduce: Reduce) {
        symbolTable.increase()
    }

    private fun fid(reduce: Reduce) {
        val token = reduce.prev
        val funcName = reduce.prev.name
        val funcType = reduce.old.name
        currentFunction = funcName
        val add2 = nextTemp()
        val dataAddress = nextDataAddress()
        val isVoid = funcType != "int"
        symbolTable.insert(token,
                Item(isVoid = isVoid, isInt = !isVoid, callbackAddress = dataAddress,
                        returnAddress = add2,
                        inputAddress = add2 + 4))
        if (funcName == "main")
            pb[0] = "(JP, $i)"

    }
}