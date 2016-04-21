import com.urbancode.air.AirPluginTool
import com.urbancode.air.plugin.Tibco.TibcoHelper

final def apTool = new AirPluginTool(args[0], args[1])
final def stepProps = apTool.getStepProperties()

final def TOPIC_LIST_START_TOKEN = 'Topic Name'
final def TOPIC_LIST_END_TOKEN = 'Command: quit'
final def TOPIC_CREATE_START_TOKEN = 'Command: create topic'
final def TOPIC_CREATE_END_TOKEN = 'Command: commit'
final def TOPIC_UPDATE_START_TOKEN = 'Command: addprop topic'
final def TOPIC_UPDATE_END_TOKEN = 'Command: commit'

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
    println "Get a list of existing topics"
    def existingTopicSet = [] as Set
    def topicListScript = tibcoHelper.createScript {Writer out ->
        out.println('show topics')
        out.println('quit')
    }

    tibcoHelper.runTibcoScript("List topics", topicListScript) { InputStream input ->
        def inList = false
        input.eachLine { line ->
            if (line != null && line.trim().length() > 0) {
                if (line.trim().startsWith(TOPIC_LIST_START_TOKEN)) {
                    inList = true
                }
                else if (line.trim().startsWith(TOPIC_LIST_END_TOKEN)) {
                    inList = false
                }
                else if (inList) {
                    existingTopicSet << line.trim().tokenize(' ')[0].trim()
                }
            }
        }
    }

    existingTopicSet.each {
        println "    $it"
    }

    def processLine = { String line ->
        if (line != null && line.trim().length() > 0) {
            println "------------------------------------------------------------"
            def topicInfo = (line.split('->', 2) as List).collectAll{it.trim()}
            if (topicInfo.size() != 2) {
                println "    invalid input line: $line"
                hasErrors = true
            }
            else {
                println "    processing topic $line"
                if (existingTopicSet.contains(topicInfo[0])) {
                    println "        topic already exists, updating properties..."
                    def topicUpdateScript = tibcoHelper.createScript {Writer out ->
                        out.println("addprop topic ${topicInfo[0]} ${topicInfo[1]}")
                        out.println('commit')
                        out.println('quit')
                    }

                    try {
                        tibcoHelper.runTibcoScript("Update Topic Properties", topicUpdateScript) { InputStream input ->
                            def inOutput = false
                            input.eachLine { ln ->
                                if (ln != null && ln.trim().length() > 0) {
                                    if (ln.trim().startsWith(TOPIC_UPDATE_START_TOKEN)) {
                                        inOutput = true
                                    }
                                    else if (ln.trim().startsWith(TOPIC_UPDATE_END_TOKEN)) {
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
                    println "        topic does not exist, creating..."
                    def topicCreateScript = tibcoHelper.createScript {Writer out ->
                        out.println("create topic ${topicInfo[0]} ${topicInfo[1]}")
                        out.println('commit')
                        out.println('quit')
                    }

                    try {
                        tibcoHelper.runTibcoScript("Create Topic", topicCreateScript) { InputStream input ->
                            def inOutput = false
                            input.eachLine { ln ->
                                if (ln != null && ln.trim().length() > 0) {
                                    if (ln.trim().startsWith(TOPIC_CREATE_START_TOKEN)) {
                                        inOutput = true
                                    }
                                    else if (ln.trim().startsWith(TOPIC_CREATE_END_TOKEN)) {
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
