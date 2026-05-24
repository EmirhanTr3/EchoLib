package cat.emir.echolib.utils

import cat.emir.echolib.EchoPlugin
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo

class ClassUtils {
    companion object {
        fun findClasses(plugin: EchoPlugin, pkg: String, condition: (ClassInfo) -> Boolean, function: (ClassInfo) -> Unit) {
            ClassGraph()
                .acceptPackages(pkg)
                .addClassLoader(plugin.javaClass.classLoader)
                .enableClassInfo()
                .scan().use { scanResult ->
                    scanResult.allClasses.forEach {
                        if (!condition(it)) return@forEach
                        function(it)
                    }
                }
        }
    }
}