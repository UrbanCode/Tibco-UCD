import com.urbancode.air.AirPluginTool;

final def out = System.out
final def apTool = new AirPluginTool(this.args[0], this.args[1])
final def stepProps = apTool.getStepProperties()

final def buildear = stepProps['buildearExec'] ? stepProps['buildearExec'] : "buildear"

final def projectDirPattern = stepProps['projectDirPattern']
final def archiveDirectory = stepProps['archiveDirectory']
final def earDirectory = stepProps['earDirectory']

def buildearDir = new File(buildear).getParentFile()

def cwd = new File(".");

cwd.eachDirMatch(~projectDirPattern) { projectDirFile ->
    println "Processing project directory ${projectDirFile}"
    def archiveDirFile = new File(projectDirFile, archiveDirectory)
    println "Processing project archive directory ${archiveDirFile}"
    def archiveFilePattern = ~/.*\.archive/
    def earDirFile = new File(earDirectory)
    earDirFile.mkdirs()
    
    def archivePath = "/" + archiveDirectory.replace(File.separator, "/") + "/"
    
    archiveDirFile.eachFileMatch(archiveFilePattern) { archiveFile ->
        def FS = System.getProperty("file.separator")
        def archiveName = archiveFile.getName().substring(0, archiveFile.getName().lastIndexOf("."))
        def earFile = new File(earDirectory, archiveName + ".ear")
        
        println "Building $earFile from archive $archiveFile"
        
        def buildearCommand = []
        buildearCommand.add(buildear)
        buildearCommand.add("-s")
        buildearCommand.add("-x")
        buildearCommand.add("-ear")
        buildearCommand.add(archivePath + archiveFile.getName())
        buildearCommand.add("-o")
        buildearCommand.add(earFile.getAbsolutePath())
        buildearCommand.add("-p")
        buildearCommand.add(projectDirFile.getAbsolutePath())
        
        println "Build ear command: " + buildearCommand.join(" ")
        
        def process = buildearCommand.execute(null, buildearDir)
        process.consumeProcessOutput(out, out)
        process.getOutputStream().close() // close stdin
        process.waitFor()
        if (process.exitValue()) {
            out.println "Failed to build earfile $earFile with archive $archiveFile and project $projectDirFile"
            System.exit(process.exitValue())
        }
        println ''
    }
}