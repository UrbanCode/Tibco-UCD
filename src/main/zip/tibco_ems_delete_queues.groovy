import com.urbancode.air.AirPluginTool
import com.urbancode.air.plugin.Tibco.TibcoHelper

final def apTool = new AirPluginTool(args[0], args[1])
final def stepProps = apTool.getStepProperties()

final def QUEUE_LIST_START_TOKEN = 'Queue Name'
final def QUEUE_LIST_END_TOKEN = 'Command: quit'
final def QUEUE_DELETE_START_TOKEN = 'Command: delete queue'
final def QUEUE_DELETE_END_TOKEN = 'Command: commit'

final def server = stepProps['server']
final def username = stepProps['username']
final def password = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
final def tibemsadminExec = stepProps['tibemsadminExec']
final def deleteList = stepProps['deleteList']?.trim()
final def deleteFileName = stepProps['deleteFile']
def File deleteFile
if (deleteFileName != null && deleteFileName.trim().length() > 0) {
    deleteFile = new File(deleteFileName)
}

println "Server: $server"
println "Username: $username"
println "tibemsadmin: ${tibemsadminExec?:''}"
println "Delete List:"
println deleteList?:''
println "Delete File: ${deleteFileName?:''}"


def tibcoHelper = new TibcoHelper(server, username, password, tibemsadminExec)
def hasErrors = false


if ((deleteList != null && deleteList.length() > 0) || (deleteFile != null && deleteFile.exists() && deleteFile.size() > 0)) {
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
            println "    processing queue $line"
            if (existingQueueSet.contains(line.trim())) {
                println "        queue exists, deleting"
                def queueDeleteScript = tibcoHelper.createScript {Writer out ->
                    out.println("delete queue ${line.trim()}")
                    out.println('commit')
                    out.println('quit')
                }

                try {
                    tibcoHelper.runTibcoScript("Delete Queue", queueDeleteScript) { InputStream input ->
                        def inOutput = false
                        input.eachLine { ln ->
                            if (ln != null && ln.trim().length() > 0) {
                                if (ln.trim().startsWith(QUEUE_DELETE_START_TOKEN)) {
                                    inOutput = true
                                }
                                else if (ln.trim().startsWith(QUEUE_DELETE_END_TOKEN)) {
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
                println "        queue does not exist!"
            }
        }
    }

    if (deleteList.trim().length() > 0) {
        println "============================================================"
        println "processing delete list:"
        deleteList.trim().eachLine { String line ->
            processLine(line)
        }
    }

    if (deleteFile != null && deleteFile.exists()) {
        println "============================================================"
        println "processing delete file ${deleteFile.canonicalPath}"
        deleteFile.eachLine {line ->
            processLine(line)
        }
    }

    if (hasErrors) {
        println "Encountered errors!!!"
        System.exit 1
    }

}
else {
    println "Nothing to delete!"
}
