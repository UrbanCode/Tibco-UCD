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

def appmanage = stepProps['appmanageExec'] ?: "AppManage"
def earfile = stepProps['earfile']
def appname = stepProps['appname']
def domain = stepProps['domain']
def additionalParameters = stepProps['additionalParameters'].split("\\s")
def username = stepProps['username']
def password = stepProps['password'] ?: stepProps['passwordscript']

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

out.println "Deploying ear command: " + formatCommandForOutput(deployCommand)

def process = deployCommand.execute()
process.consumeProcessOutput(out,out)
process.getOutputStream().close() // close stdin
process.waitFor()
if (process.exitValue()) {
    println "Failed to deploy $earfile to $appname in $domain"
}
System.exit(process.exitValue())
