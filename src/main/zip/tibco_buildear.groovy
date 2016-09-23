import com.urbancode.air.AirPluginTool;

String formatCommandForOutput(def command) {
    def output = new StringBuilder()
    for(String element : command) {
        output.append(element)
        output.append(" ")
    }
    return output.toString()
}

final def out = System.out

final def apTool = new AirPluginTool(this.args[0], this.args[1])
final def stepProps = apTool.getStepProperties()

final def buildear = stepProps['buildearExec'] ?: "buildear"
final def archive = stepProps['archive']
final def earfile = new File(stepProps['earfile'])
final def project = new File(stepProps['project'])

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
