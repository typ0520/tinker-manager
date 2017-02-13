package com.dx168.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * tinker jiagu support
 * Created by tong on 17/2/5.
 */
class TinkerJiaguPlugin implements Plugin<Project>  {
    void apply(Project project) {
        project.afterEvaluate {
            def android = project.extensions.android
            android.applicationVariants.all { variant ->

                def variantOutput = variant.outputs.first()
                def variantName = variant.name.capitalize()
                project.task("tinkerPatch${variantName}Jiagu", group: 'tinker',dependsOn: ["tinkerPatch${variantName}"]) {
                    doLast {
                        println "variantOutput: ${variantOutput}"

                        def outputFolder = null
                        variant.outputs.each { output ->
                            outputFolder = "${output.outputFile.getParentFile().getParentFile().getAbsolutePath()}/tinkerPatch/${variant.dirName}"
                        }

                        println("outputFolder: ${new File(new File(outputFolder),"patch_signed.apk")}")

                        File fullDexDir = new File(outputFolder,"tinker_patch_jiagu")
                        if (fullDexDir.exists() && fullDexDir.isDirectory()) {
                            if (!fullDexDir.delete()) {
                                throw new RuntimeException("Failed to delete directory, " + fullDexDir.absolutePath)
                            }
                        }
                        if (!fullDexDir.mkdirs()) {
                            throw new RuntimeException("Failed to create directory, " + fullDexDir.absolutePath)
                        }
                        //创建全量dex补丁包的标记文件
                        def fullDexMarkerFile = new File(fullDexDir,"FULL_PATCH")
                        if (!fullDexMarkerFile.createNewFile()) {
                            throw new RuntimeException("Failed to create the markup file, " + fullDexDir.absolutePath)
                        }
                        //把原始补丁文件放进去
                        project.copy {
                            from new File(new File(outputFolder),"patch_signed.apk")
                            rename 'patch_signed.apk','patch.apk'
                            into fullDexDir
                        }
                        //copy 全量dex
                        def dexContainerDir = new File(outputFolder,project.name + "-" + variantName)
                        project.copy {
                            from new File(dexContainerDir,"classes.dex")
                            into fullDexDir
                        }
//mock code
//                        new File(dexContainerDir,"classes2.dex").createNewFile()
//                        new File(dexContainerDir,"classes3.dex").createNewFile()
                        File dexFile = null;
                        for (int point = 2;(dexFile = new File(dexContainerDir,"classes" + point + ".dex")).exists();point++) {
                            project.copy {
                                from dexFile
                                into fullDexDir
                            }
                        }

                        //生成全量补丁包
                        //project.ant.zip(baseDir: fullDexDir, destFile: new File(outputFolder,"patch_signed_with_full.apk"))
                        def fullPatch = new File(outputFolder,"patch_signed_jiagu.apk")
                        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fullPatch))
                        File[] files = fullDexDir.listFiles()
                        for (File file : files) {
                            ZipEntry e = new ZipEntry(file.name)
                            out.putNextEntry(e)
                            byte[] bytes = readFile(file)
                            out.write(bytes,0,bytes.length)
                            out.closeEntry()
                        }
                        out.close()
                    }
                }
            }
        }
    }

    byte[] readFile(File file) {
        InputStream fis = new FileInputStream(file)
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        int len = -1
        byte[] buffer = new byte[1024]
        while ((len = fis.read(buffer)) != -1) {
            bos.write(buffer,0,len)
        }
        fis.close()
        return bos.toByteArray()
    }
}