package optikal.optics

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

fun Messager.log(message: String) {
    this.printMessage(Diagnostic.Kind.NOTE, message)
}

fun Messager.logW(message: String) {
    this.printMessage(Diagnostic.Kind.WARNING, message)
}

fun Messager.logMW(message: String) {
    this.printMessage(Diagnostic.Kind.MANDATORY_WARNING, message)
}

fun Messager.logE(message: String, vararg args: Any) {
    var formattedMsg = message
    if (args.isNotEmpty()) {
        formattedMsg = String.format(message, args)
    }
    this.printMessage(Diagnostic.Kind.ERROR, formattedMsg)
}

fun Messager.logE(element: Element, message: String, vararg args: Any) {
    var formattedMsg = message
    if (args.isNotEmpty()) {
        formattedMsg = String.format(message, args)
    }
    this.printMessage(Diagnostic.Kind.ERROR, formattedMsg, element)
}