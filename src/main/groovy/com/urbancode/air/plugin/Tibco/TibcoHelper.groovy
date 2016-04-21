package com.urbancode.air.plugin.Tibco

import com.urbancode.air.CommandHelper

/**
 * @author Varban Vassilev (vvv@urbancode.com)
 */

public class TibcoHelper {

    def server
    def user
    def password
    def path
    def CommandHelper cmdHelper

    public TibcoHelper(server, user, password, path) {
        this.server = server
        this.user = user
        this.password = password
        this.path = path?:'tibemsadmin'
        cmdHelper = new CommandHelper(new File('.').canonicalFile)
    }

    public File createScript (Closure closure) {
        def file = File.createTempFile('ud-tibco-', '.txt')
        file.deleteOnExit()
        file.withWriter(closure)
        return file
    }

    public void runTibcoScript(String commandMessage, File script, Closure closure) {
        if (script != null && script.exists()) {
            def command = [path]
            if (server != null && server.trim().length() > 0) {
                command << '-server'
                command << server.trim()
            }
            if (user != null && user.trim().length() > 0) {
                command << '-user'
                command << user.trim()
            }
            if (password != null && password.trim().length() > 0) {
                command << '-password'
                command << password.trim()
            }

            command << '-script'
            command << script.canonicalPath

            if (closure == null) {
                cmdHelper.runCommand(commandMessage, command)
            }
            else {
                cmdHelper.runCommand(commandMessage, command) { Process proc ->
                    proc.out.close()
                    proc.consumeProcessErrorStream(System.out)
                    closure(proc.in)
                }
            }
        }
        else {
            throw new Exception("tibemsadmin script ${script?.canonicalPath} does not exist!")
        }
    }
}