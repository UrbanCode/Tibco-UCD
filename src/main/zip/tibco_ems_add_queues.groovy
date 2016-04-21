import com.urbancode.air.AirPluginTool
import com.urbancode.air.plugin.Tibco.TibcoHelper

final def apTool = new AirPluginTool(args[0], args[1])
final def stepProps = apTool.getStepProperties()

final def QUEUE_LIST_START_TOKEN = 'Queue Name'
final def QUEUE_LIST_END_TOKEN = 'Command: quit'
final def QUEUE_CREATE_START_TOKEN = 'Command: create queue'
final def QUEUE_CREATE_END_TOKEN = 'Command: commit'
final def QUEUE_UPDATE_START_TOKEN = 'Command: addprop queue'
final def QUEUE_UPDATE_END_TOKEN = 'Command: commit'

final def server = stepProps['server']
final def username = stepProps['username']
final def password = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
final def tibemsadminExec = stepProps['tibemsadminExec']
final def updateList = stepProps['updateList']?.trim()
final def updateFileName = stepProps['updateFile']
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
    println "Get a list of existing queues"
    def existingQueueSet = [] as Set
    def queueListScript = tibcoHelper.createScript {Writer out ->
        out.println('show queues')
        out.println('quit')
    }

    tibcoHelper.runTibcoScript("List queues", queueListScript) { InputStream input ->
        def inList = false
        input.eachLine { line ->
            if (line != null && line.trim().length() > 0) {
                if (line.trim().startsWith(QUEUE_LIST_START_TOKEN)) {
                    inList = true
                }
                else if (line.trim().startsWith(QUEUE_LIST_END_TOKEN)) {
                    inList = false
                }
                else if (inList) {
                    existingQueueSet << line.trim().tokenize(' ')[0].trim()
                }
            }
        }
    }

    existingQueueSet.each {
        println "    $it"
    }

    def processLine = { String line ->
        if (line != null && line.trim().length() > 0) {
            println "------------------------------------------------------------"
            def queueInfo = (line.split('->', 2) as List).collectAll{it.trim()}
            if (queueInfo.size() != 2) {
                println "    invalid input line: $line"
                hasErrors = true
            }
            else {
                println "    processing queue $line"
                if (existingQueueSet.contains(queueInfo[0])) {
                    println "        queue already exists, updating properties..."
                    def queueUpdateScript = tibcoHelper.createScript {Writer out ->
                        out.println("addprop queue ${queueInfo[0]} ${queueInfo[1]}")
                        out.println('commit')
                        out.println('quit')
                    }

                    try {
                        tibcoHelper.runTibcoScript("Update Queue Properties", queueUpdateScript) { InputStream input ->
                            def inOutput = false
                            input.eachLine { ln ->
                                if (ln != null && ln.trim().length() > 0) {
                                    if (ln.trim().startsWith(QUEUE_UPDATE_START_TOKEN)) {
                                        inOutput = true
                                    }
                                    else if (ln.trim().startsWith(QUEUE_UPDATE_END_TOKEN)) {
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
                else {
                    println "        queue does not exist, creating..."
                    def queueCreateScript = tibcoHelper.createScript {Writer out ->
                        out.println("create queue ${queueInfo[0]} ${queueInfo[1]}")
                        out.println('commit')
                        out.println('quit')
                    }

                    try {
                        tibcoHelper.runTibcoScript("Create Queue", queueCreateScript) { InputStream input ->
                            def inOutput = false
                            input.eachLine { ln ->
                                if (ln != null && ln.trim().length() > 0) {
                                    if (ln.trim().startsWith(QUEUE_CREATE_START_TOKEN)) {
                                        inOutput = true
                                    }
                                    else if (ln.trim().startsWith(QUEUE_CREATE_END_TOKEN)) {
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
