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

final def appmanage = stepProps['appmanageExec'] ? stepProps['appmanageExec'] : "AppManage"
final def earfile = stepProps['earfile']
final def appname = stepProps['appname']
final def domain = stepProps['domain']
final def additionalParameters = stepProps['additionalParameters'].split("\\s")
final def username = stepProps['username']
final def password = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']

def deployCommand = []
deployCommand.add(appmanage)
deployCommand.add("-deploy")
deployCommand.add("-ear")
deployCommand.add(earfile)
deployCommand.add("-app")
deployCommand.add(appname)
deployCommand.add("-domain")
deployCommand.add(domain)
deployCommand.add("-user")
deployCommand.add(username)
deployCommand.add("-pw")
deployCommand.add(password)
deployCommand.add("-force")
for (def param in additionalParameters) {
    if (param.trim()) deployCommand.add(param)
}

out.println "Build ear command: " + formatCommandForOutput(deployCommand)

def process = deployCommand.execute()
process.consumeProcessOutput(out,out)
process.getOutputStream().close() // close stdin
process.waitFor()
if (process.exitValue()) {
    println "Failed to deploy $earfile to $appname in $domain"
}
System.exit(process.exitValue())