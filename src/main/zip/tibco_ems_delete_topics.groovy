import com.urbancode.air.plugin.Tibco.TibcoHelper
import com.urbancode.air.plugin.helpers.NewAirPluginTool

def apTool = new NewAirPluginTool(args[0], args[1])
def stepProps = apTool.getStepProperties()

def TOPIC_LIST_START_TOKEN = 'Topic Name'
def TOPIC_LIST_END_TOKEN = 'Command: quit'
def TOPIC_DELETE_START_TOKEN = 'Command: delete topic'
def TOPIC_DELETE_END_TOKEN = 'Command: commit'

def server = stepProps['server']
def username = stepProps['username']
def password = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
def tibemsadminExec = stepProps['tibemsadminExec']
def deleteList = stepProps['deleteList']?.trim()
def deleteFileName = stepProps['deleteFile']

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
            println "    processing topic $line"
            if (existingTopicSet.contains(line.trim())) {
                println "        topic exists, deleting"
                def topicDeleteScript = tibcoHelper.createScript {Writer out ->
                    out.println("delete topic ${line.trim()}")
                    out.println('commit')
                    out.println('quit')
                }

                try {
                    tibcoHelper.runTibcoScript("Delete Topic", topicDeleteScript) { InputStream input ->
                        def inOutput = false
                        input.eachLine { ln ->
                            if (ln != null && ln.trim().length() > 0) {
                                if (ln.trim().startsWith(TOPIC_DELETE_START_TOKEN)) {
                                    inOutput = true
                                }
                                else if (ln.trim().startsWith(TOPIC_DELETE_END_TOKEN)) {
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
                println "        topic does not exist!"
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
