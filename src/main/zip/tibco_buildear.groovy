import com.urbancode.air.plugin.helpers.NewAirPluginTool

String formatCommandForOutput(def command) {
    def output = new StringBuilder()
    for(String element : command) {
        output.append(element)
        output.append(" ")
    }
    return output.toString()
}

def out = System.out

def apTool = new NewAirPluginTool(this.args[0], this.args[1])
def stepProps = apTool.getStepProperties()

def buildear = stepProps['buildearExec'] ?: "buildear"
def archive = stepProps['archive']
def earfile = new File(stepProps['earfile'])
def project = new File(stepProps['project'])

def buildearDir = new File(buildear).getParentFile()

def buildearCommand = []
buildearCommand.add(buildear)
buildearCommand.add("-s")
if (archive) {
    buildearCommand.add("-ear")
    buildearCommand.add(archive)
}
buildearCommand.add("-o")
buildearCommand.add(earfile.getAbsolutePath())
buildearCommand.add("-p")
buildearCommand.add(project.getAbsolutePath())

println "Build ear command: " + formatCommandForOutput(buildearCommand)

def process = buildearCommand.execute(null, buildearDir)
process.consumeProcessOutput(out,out)
process.getOutputStream().close() // close stdin
process.waitFor()
if (process.exitValue()) {
    println "Failed to build earfile $earfile with archive $archive and project $project"
}
System.exit(process.exitValue())
