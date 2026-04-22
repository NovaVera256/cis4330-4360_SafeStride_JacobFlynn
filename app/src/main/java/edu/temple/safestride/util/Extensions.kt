package edu.temple.safestride.util

fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)
fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)