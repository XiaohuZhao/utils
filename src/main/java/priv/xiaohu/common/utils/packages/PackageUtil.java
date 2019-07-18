package priv.xiaohu.common.utils.packages;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static priv.xiaohu.common.utils.object.ObjectUtil.isNotEmpty;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;

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
    public static List<String> getClassNames(final String packageName) throws IOException {
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
    public static List<String> getClassNames(final String packageName, final boolean childPackage) throws IOException {
        final List<String> fileNames = new ArrayList<>();
        final ClassLoader loader = currentThread().getContextClassLoader();
        final String packagePath = packageName.replace(".", "/");
        final Enumeration<URL> urls = loader.getResources(packagePath);
        while (urls.hasMoreElements()) {
            final URL url = urls.nextElement();
            if (url == null) {
                continue;
            }
            final String type = url.getProtocol();
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
    private static List<String> getClassNameByFile(final String filePath, final boolean childPackage) {
        final List<String> myClassName = new ArrayList<>();
        final File file = new File(filePath);
        final File[] childFiles = file.listFiles();
        if (childFiles == null) {
            return myClassName;
        }
        for (final File childFile : childFiles) {
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
    private static List<String> getClassNameByJar(final String jarPath, final boolean childPackage) throws IOException {
        final List<String> myClassName = new ArrayList<>();
        final String[] jarInfo = jarPath.split("!");
        final String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        final String packagePath = jarInfo[1].substring(1);
        final JarFile jarFile = new JarFile(jarFilePath);
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();
            String entryName = jarEntry.getName();
            if (entryName.endsWith(".class")) {
                if (childPackage) {
                    if (entryName.startsWith(packagePath)) {
                        entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                        myClassName.add(entryName);
                    }
                } else {
                    final int index = entryName.lastIndexOf("/");
                    final String myPackagePath;
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
    private static List<String> getClassNameByJars(final URL[] urls, final String packagePath, final boolean childPackage) throws IOException {
        final List<String> myClassName = new ArrayList<>();
        if (isNotEmpty(urls)) {
            for (final URL url : urls) {
                final String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith(format("classes%s", File.separator))) {
                    continue;
                }
                final String jarPath = format("%s!%s%s", urlPath, File.separator, packagePath);
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }
}