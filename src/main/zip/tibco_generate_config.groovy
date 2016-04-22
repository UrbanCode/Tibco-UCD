import com.urbancode.air.AirPluginTool

String formatCommand(def command) {
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
final def config = stepProps['config']
final def earfile = stepProps['earfile']
final def output = stepProps['output']

def generateConfigCommand = []
generateConfigCommand.add(appmanage)
generateConfigCommand.add("-export")
generateConfigCommand.add("-ear")
generateConfigCommand.add(earfile)
generateConfigCommand.add("-deployconfig")
generateConfigCommand.add(config)
generateConfigCommand.add("-out")
generateConfigCommand.add(output)

out.println "Build ear command: " + formatCommand(generateConfigCommand)

def process = generateConfigCommand.execute()
process.consumeProcessOutput(out,out)
process.getOutputStream().close() // close stdin
process.waitFor()
if(process.exitValue()) throw new Exception("Generate config failed") // ADD EXCEPTION MESSAGE HERE