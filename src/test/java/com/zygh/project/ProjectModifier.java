package com.zygh.project;

import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * 项目修改器，一键替换 Maven 的 groupId、artifactId，项目的 package
 *
 * Created by ZhuHongYu on 2024/12/17.
 */
public class ProjectModifier {
    // 旧项目的配置
    private static final String OLD_GROUP_ID = "com.zygh";
    private static final String OLD_ARTIFACT_ID = "project-template";
    private static final String OLD_PACKAGE = "com.zygh.project";

    // 新项目的配置
    private static final String NEW_GROUP_ID = "com.newgroup";
    private static final String NEW_ARTIFACT_ID = "new-artifact";
    private static final String NEW_PACKAGE = "com.newgroup";

    // 项目路径
    private static final String SOURCE_PROJECT_DIR = getProjectBaseDir();
    private static final String OUTPUT_PROJECT_DIR = getProjectBaseDir() + "-new";

    public static void main(String[] args) throws IOException {
        System.out.println("开始生成新的 Spring Boot 项目...");

        // 1. 复制项目到新目录
        copyProject(SOURCE_PROJECT_DIR, OUTPUT_PROJECT_DIR);

        // 2. 替换 pom.xml 中的 groupId 和 artifactId
        replacePomXml(Paths.get(OUTPUT_PROJECT_DIR));

        // 3. 替换代码中的包名
        replacePackageNames(Paths.get(OUTPUT_PROJECT_DIR));

        // 4. 重命名包路径
        renamePackageFolders(Paths.get(OUTPUT_PROJECT_DIR));

        System.out.println("新项目生成完成，路径: " + OUTPUT_PROJECT_DIR);
    }

    private static String getProjectBaseDir() {
        String baseDir = System.getProperty("user.dir");
        if (StrUtil.isEmpty(baseDir)) {
            throw new NullPointerException("项目基础路径不存在");
        }
        return baseDir;
    }

    // 1. 复制整个项目目录
    private static void copyProject(String sourceDir, String targetDir) throws IOException {
        System.out.println("复制项目文件...");
        Path sourcePath = Paths.get(sourceDir);
        Path targetPath = Paths.get(targetDir);

        if (Files.exists(targetPath)) {
            System.err.println("目标目录已存在，无法覆盖: " + targetDir);
            System.exit(1);
        }

        try (Stream<Path> stream = Files.walk(sourcePath)) {
            stream.forEach(source -> {
                Path target = targetPath.resolve(sourcePath.relativize(source));
                try {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("复制失败: " + source);
                }
            });
        }
    }

    // 2. 替换 pom.xml 中的 groupId 和 artifactId
    private static void replacePomXml(Path projectPath) throws IOException {
        System.out.println("修改 pom.xml 文件...");
        try (Stream<Path> paths = Files.walk(projectPath)) {
            paths.filter(path -> path.getFileName().toString().equals("pom.xml"))
                    .forEach(pomFile -> {
                        try {
                            String content = new String(Files.readAllBytes(pomFile));
                            content = content.replace("<groupId>" + OLD_GROUP_ID + "</groupId>", "<groupId>" + NEW_GROUP_ID + "</groupId>");
                            content = content.replace("<artifactId>" + OLD_ARTIFACT_ID + "</artifactId>", "<artifactId>" + NEW_ARTIFACT_ID + "</artifactId>");
                            Files.write(pomFile, content.getBytes());
                            System.out.println("已修改: " + pomFile);
                        } catch (IOException e) {
                            System.err.println("修改失败: " + pomFile);
                        }
                    });
        }
    }

    // 3. 替换所有代码文件中的包名
    private static void replacePackageNames(Path projectPath) throws IOException {
        System.out.println("替换代码中的包名...");
        try (Stream<Path> paths = Files.walk(projectPath)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .forEach(javaFile -> {
                        try {
                            String content = new String(Files.readAllBytes(javaFile));
                            content = content.replace(OLD_PACKAGE, NEW_PACKAGE);
                            Files.write(javaFile, content.getBytes());
                            System.out.println("已修改: " + javaFile);
                        } catch (IOException e) {
                            System.err.println("替换失败: " + javaFile);
                        }
                    });
        }
    }

    // 4. 重命名包路径
    private static void renamePackageFolders(Path projectPath) throws IOException {
        System.out.println("重命名包路径...");
        Path oldPath = projectPath.resolve("src/main/java/" + OLD_PACKAGE.replace(".", "/"));
        Path newPath = projectPath.resolve("src/main/java/" + NEW_PACKAGE.replace(".", "/"));

        if (Files.exists(oldPath)) {
            Files.createDirectories(newPath.getParent()); // 确保父目录存在
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("包路径已重命名: " + oldPath + " -> " + newPath);
        } else {
            System.err.println("旧包路径不存在: " + oldPath);
        }
    }
}
