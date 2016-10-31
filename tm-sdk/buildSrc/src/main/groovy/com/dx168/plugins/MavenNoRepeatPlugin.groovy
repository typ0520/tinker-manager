package com.dx168.plugins

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

/**
 * 检查jar包版本不允许重复,上传包以后
 * Created by tong on 16/7/13.
 */
class MavenNoRepeatPlugin implements Plugin<Project>,TaskExecutionListener,BuildListener  {
    private static final String SCRIPT_NAME = "upload-tag.sh";

    private Project bootProject;

    void apply(Project project) {
        println("####" + project.name)
        this.bootProject = project;
        project.gradle.addListener(this)

        project.task('dxupload', group: 'upload',dependsOn: ['uploadArchives']) {
            doLast {
                println('成功上传到maven仓库,开始打tag')
                //上传tag
                checkScriptFile(project.rootProject.buildDir)
                String cmd = "sh ${project.rootProject.buildDir}/${SCRIPT_NAME} v-${project.rootProject.version} v-${project.rootProject.version}"
                println(cmd)
                def process = cmd.execute()
                int status = process.waitFor()
                if (status == 0) {
                    String exeResult = process.in.text
                    println(exeResult)
                }
                process.destroy()
            }
        }
    }

    @Override
    void beforeExecute(Task task) {
        if (task.project != bootProject) {
            return
        }
        if (!'uploadArchives'.equals(task.name)) {
            return
        }

        //判断是否是snapshot
        String version = bootProject.version;
        if (version == null || version.toUpperCase().endsWith("SNAPSHOT")) {
            //snapshot不处理
            return
        }

        println('==start uploadArchives: ' + bootProject.version)

        String url = bootProject.mavenServer + bootProject.mavenReleases
        if (!url.endsWith("/")) {
            url = url + "/"
        }
        url = url + bootProject.rootProject.name.replaceAll("\\.","/") + "/" + bootProject.name.replaceAll("\\.","/") + "/" + bootProject.version;
        println('==检查' + bootProject.rootProject.name + ":" + bootProject.name + ":" + bootProject.version + "在maven服务器上是否存在" + url)

        HttpURLConnection conn = new URL(url).openConnection()
        conn.setRequestMethod("HEAD")
        conn.connect()

        int resCode = conn.getResponseCode()

        println('==response code: ' + resCode)
        if (resCode != 200 && resCode != 404) {
            throw new RuntimeException("请求失败: " + url)
        }
        if (resCode != 404) {
            throw new RuntimeException("maven服务器上已存在该版本不允许覆盖: " + url)
        }
    }

    @Override
    void afterExecute(Task task, TaskState state) {
        if (!'uploadArchives'.equals(task.name)) {
            return
        }

        //判断是否是snapshot
        String version = bootProject.version;
        if (version == null || version.toUpperCase().endsWith("SNAPSHOT")) {
            //snapshot不处理
            return
        }

        println('==finish uploadArchives: ' + task)
    }

    @Override
    void buildStarted(Gradle gradle) {

    }

    @Override
    void settingsEvaluated(Settings settings) {

    }

    @Override
    void projectsLoaded(Gradle gradle) {

    }

    @Override
    void projectsEvaluated(Gradle gradle) {

    }

    @Override
    void buildFinished(BuildResult buildResult) {
        //println('==buildFinished buildFinished: ' + buildResult.getAction() + " " + buildResult.getFailure())
    }

    void checkScriptFile(File buildDir) {
        File script = new File(buildDir,SCRIPT_NAME)

        if (script.exists() && script.isFile()) {
            return
        }

        /**

         #!/usr/bin/env bash

         version=$1
         desc=$2
         if [ -z $version ] || [ -z $desc ];then
         exit -1
         fi

         echo "${version} = ${desc}"

         #检测tag是否存在
         git tag | grep "${version}" > /dev/null 2>&1
         if [ $? == 0 ];then
         git tag -d "${version}"
         git push --delete origin "${version}" > /dev/null 2>&1
         fi
         git tag -a "${version}" -m "${desc}"
         git push origin --tags
         */

        def printWriter = script.newPrintWriter()
        printWriter.write('#!/usr/bin/env bash\n' +
                '\n' +
                'version=$1\n' +
                'desc=$2\n' +
                'if [ -z $version ] || [ -z $desc ];then\n' +
                '    exit -1\n' +
                'fi\n' +
                '\n' +
                '#检测tag是否存在\n' +
                'git tag | grep "${version}" > /dev/null 2>&1\n' +
                'if [ $? == 0 ];then\n' +
                '    git tag -d "${version}"\n' +
                '    git push --delete origin "${version}" > /dev/null 2>&1\n' +
                'fi\n' +
                'git tag -a "${version}" -m "${desc}"\n' +
                'git push origin --tags\n' +
                '\n' +
                'git tag')
        printWriter.flush()
        printWriter.close()
    }
}