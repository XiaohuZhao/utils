package com.yorma.common.utils.packages;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author yangying
 */
@SuppressWarnings("unused")
public class PackageUtil {

    /**
     * 获取某包下（包括该包的所有子包）所有类
     *
     * @param packageName 包名
     * @return 类的完整名称
     * @throws IOException 文件读取失败
     */
    public static Set<String> getClassNames(String packageName) throws IOException {
        return getClassNames(packageName, true);
    }

    /**
     * 获取某包下所有类
     *
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     * @throws IOException 文件读取失败
     */
    public static Set<String> getClassNames(String packageName, boolean childPackage) throws IOException {
        Set<String> fileNames = new HashSet<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        Enumeration<URL> urls = loader.getResources(packagePath);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url == null) {
                continue;
            }
            String type = url.getProtocol();
            if ("file".equals(type)) {
                fileNames.addAll(getClassNameByFile(url.getPath(), childPackage));
            } else if ("jar".equals(type)) {
                fileNames.addAll(getClassNameByJar(url.getPath(), childPackage));
            }
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     *
     * @param filePath     文件路径
     *                     类名集合
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameByFile(String filePath, boolean childPackage) {
        Set<String> myClassName = new HashSet<>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        if (childFiles == null) {
            return myClassName;
        }
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), childPackage));
                }
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.replace(File.separator, ".");
                    childFilePath = childFilePath.substring(childFilePath.indexOf(".classes.") + 9, childFilePath.lastIndexOf("."));
                    myClassName.add(childFilePath);
                }
            }
        }
        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     *
     * @param jarPath      jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameByJar(String jarPath, boolean childPackage) throws IOException {
        Set<String> myClassName = new HashSet<>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String entryName = jarEntry.getName();
            if (entryName.endsWith(".class")) {
                if (childPackage) {
                    if (entryName.startsWith(packagePath)) {
                        entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                        myClassName.add(entryName);
                    }
                } else {
                    int index = entryName.lastIndexOf("/");
                    String myPackagePath;
                    if (index != -1) {
                        myPackagePath = entryName.substring(0, index);
                    } else {
                        myPackagePath = entryName;
                    }
                    if (myPackagePath.equals(packagePath)) {
                        entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                        myClassName.add(entryName);
                    }
                }
            }
        }

        return myClassName;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls         URL集合
     * @param packagePath  包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) throws IOException {
        Set<String> myClassName = new HashSet<>();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith(String.format("classes%s", File.separator))) {
                    continue;
                }
                String jarPath = String.format("%s!%s%s", urlPath, File.separator, packagePath);
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }
}