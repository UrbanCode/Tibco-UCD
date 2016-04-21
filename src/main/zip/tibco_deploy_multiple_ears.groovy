import com.urbancode.air.AirPluginTool;

final def out = System.out
final def apTool = new AirPluginTool(this.args[0], this.args[1])
final def stepProps = apTool.getStepProperties()

final def earDirectory = stepProps['earDirectory']
final def appPath = stepProps['appPath']
final def domain = stepProps['domain']
final def appmanage = stepProps['appmanageExec'] ? stepProps['appmanageExec'] : "AppManage"
final def username = stepProps['username']
final def password = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']

def earDirFile = new File(earDirectory);
def earFilePattern = ~/.*\.ear/

if (!appPath.endsWith("/")) {
    appPath += "/"
}

earDirFile.eachFileMatch(earFilePattern) { earFile ->
    def appname = appPath + earFile.getName().substring(0, earFile.getName().lastIndexOf("."))
    
    def deployCommand = []
    deployCommand.add(appmanage)
    deployCommand.add("-deploy")
    deployCommand.add("-ear")
    deployCommand.add(earFile.getAbsolutePath())
    deployCommand.add("-app")
    deployCommand.add(appname)
    deployCommand.add("-domain")
    deployCommand.add(domain)
    deployCommand.add("-user")
    deployCommand.add(username)
    deployCommand.add("-pw")
    deployCommand.add(password)
    deployCommand.add("-force")
    
    println "Deploying ear $earFile of application $appname to domain $domain"
    
    println "Deploy ear command: " + deployCommand.join(" ")
    
    def process = deployCommand.execute()
    process.consumeProcessOutput(out, out)
    process.getOutputStream().close() // close stdin
    process.waitFor()
    if (process.exitValue()) {
        println "Deployment of $earFile failed."
        System.exit(process.exitValue())
    }
    println ''
}

