import com.urbancode.air.plugin.Tibco.TibcoHelper
import com.urbancode.air.plugin.helpers.NewAirPluginTool

def apTool = new NewAirPluginTool(args[0], args[1])
def stepProps = apTool.getStepProperties()

def JNDINAME_LIST_START_TOKEN = 'JNDI Name'
def JNDINAME_LIST_END_TOKEN = 'Command: quit'
def JNDINAME_CREATE_START_TOKEN = 'Command: create jndiname'
def JNDINAME_CREATE_END_TOKEN = 'Command: commit'

def server = stepProps['server']
def username = stepProps['username']
def password = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
def tibemsadminExec = stepProps['tibemsadminExec']
def updateList = stepProps['updateList']?.trim()
def updateFileName = stepProps['updateFile']

def File updateFile
if (updateFileName != null && updateFileName.trim().length() > 0) {
    updateFile = new File(updateFileName)
}

println "Server: $server"
println "Username: $username"
println "tibemsadmin: ${tibemsadminExec?:''}"
println "Update List:"
println updateList?:''
println "Update File: ${updateFileName?:''}"


def tibcoHelper = new TibcoHelper(server, username, password, tibemsadminExec)
def hasErrors = false


if ((updateList != null && updateList.length() > 0) || (updateFile != null && updateFile.exists() && updateFile.size() > 0)) {
    println "============================================================"
    println "Get a list of existing jndinames"
    def existingJndinameSet = [] as Set
    def jndinameListScript = tibcoHelper.createScript {Writer out ->
        out.println('show jndinames')
        out.println('quit')
    }

    tibcoHelper.runTibcoScript("List jndinames", jndinameListScript) { InputStream input ->
        def inList = false
        input.eachLine { line ->
            if (line != null && line.trim().length() > 0) {
                if (line.trim().startsWith(JNDINAME_LIST_START_TOKEN)) {
                    inList = true
                }
                else if (line.trim().startsWith(JNDINAME_LIST_END_TOKEN)) {
                    inList = false
                }
                else if (inList) {
                    existingJndinameSet << line.trim().tokenize(' ')[0].trim()
                }
            }
        }
    }

    existingJndinameSet.each {
        println "    $it"
    }

    def processLine = { String line ->
        if (line != null && line.trim().length() > 0) {
            println "------------------------------------------------------------"
            def jndinameInfo = (line.split('->', 2) as List).collectNested{it.trim()}
            if (jndinameInfo.size() != 2) {
                println "    invalid input line: $line"
                hasErrors = true
            }
            else {
                println "    processing jndiname: $line"
                if (existingJndinameSet.contains(jndinameInfo[0])) {
                    println "        jndiname already exists!"
                }
                else {
                    println "        jndiname does not exist, creating..."
                    def jndinameCreateScript = tibcoHelper.createScript {Writer out ->
                        out.println("create jndiname ${jndinameInfo[0]} ${jndinameInfo[1]}")
                        out.println('commit')
                        out.println('quit')
                    }

                    try {
                        tibcoHelper.runTibcoScript("Create JNDI Name", jndinameCreateScript) { InputStream input ->
                            def inOutput = false
                            input.eachLine { ln ->
                                if (ln != null && ln.trim().length() > 0) {
                                    if (ln.trim().startsWith(JNDINAME_CREATE_START_TOKEN)) {
                                        inOutput = true
                                    }
                                    else if (ln.trim().startsWith(JNDINAME_CREATE_END_TOKEN)) {
                                        inOutput = false
                                    }
                                    else if (inOutput) {
                                        println "            $ln"
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        hasErrors = true
                        println e.message
                    }
                }
            }
        }
    }

    if (updateList.trim().length() > 0) {
        println "============================================================"
        println "processing update list:"
        updateList.trim().eachLine { String line ->
            processLine(line)
        }
    }

    if (updateFile != null && updateFile.exists()) {
        println "============================================================"
        println "processing update file ${updateFile.canonicalPath}"
        updateFile.eachLine {line ->
            processLine(line)
        }
    }

    if (hasErrors) {
        println "Encountered errors!!!"
        System.exit 1
    }

}
else {
    println "Nothing to update!"
}
