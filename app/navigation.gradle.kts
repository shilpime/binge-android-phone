import java.io.BufferedReader
import java.util.regex.Matcher
import java.util.regex.Pattern

val MAGIC_TEXT = "{@string/app_intent_host}"
val MAGIC_TEXT_2 = "{@string/old_app_intent_host}"

        tasks.register("convertNavigation") {
            doLast{
                val hostNameUat: String by project
                val hostNameUatOld: String by project
                val hostNameProd: String by project
                val hostNameProdOld: String by project
                val hostNameToUse : String = if(getCurrentFlavor().equals("uat", true))  hostNameUat else hostNameProd
                val hostNameToUseOld : String = if(getCurrentFlavor().equals("uat", true))  hostNameUatOld else hostNameProdOld

                val generatedResDir = File(buildDir, "generated/local/main/res")
                val targetDir = File(  generatedResDir, "navigation")

                if (targetDir.exists()) {
                    targetDir.delete()
                }
                targetDir.mkdirs()

                val tree = fileTree("src/main/nav/navigation")
                val host: String = if (!hostNameToUse.isBlank()) hostNameToUse else "tataplaybinge.com"
                val hostOld: String = if (!hostNameToUseOld.isBlank()) hostNameToUseOld else "tataplaybinge.com"

                println ("Replacing $MAGIC_TEXT in nav files with [$host]")

                for (f in tree) {
                    val target = File(targetDir, f.name)
                    println ("Transforming ${f.name} to ${target.absolutePath}")
                    val writer = target.writer()
                    val allText = f.absoluteFile.inputStream().bufferedReader().use(BufferedReader::readText)
                    val newText = duplicateDeeplink(allText)
                    writer.write(newText.replace(MAGIC_TEXT, host).replace(MAGIC_TEXT_2, hostOld))
                    writer.close()
                }
            }
        }
        fun duplicateDeeplink(fileText:String):String {
            var regex = Pattern.compile("\\<deepLink(.*?)\\/>", Pattern.DOTALL)
            var matcher: Matcher = regex.matcher(fileText)
            var f = fileText
            var insertedLength = 0
            matcher.results().forEach {
                val wwwDeeplink = "\n" + it.group()
                    .replace(MAGIC_TEXT, MAGIC_TEXT_2) + "\n"
                f = StringBuffer(f).insert(it.end() + insertedLength, wwwDeeplink).toString()
                insertedLength += wwwDeeplink.length
            }
            return f
        }

        fun getCurrentFlavor() :String {
            var tskReqStr = gradle.getStartParameter().getTaskRequests().toString()
            val pattern : Pattern = if (tskReqStr.contains("assemble"))
                Pattern.compile("assemble(\\w+)(Release|Debug)")
            else
                Pattern.compile("generate(\\w+)(Release|Debug)")
            val matcher = pattern.matcher(tskReqStr)
            if (matcher.find())
                return matcher.group(1).toLowerCase()
            else {
                println("NO MATCH FOUND")
                return ""
            }
        }