package org.example.ui;

public class JavaVersionCheck {
    public static void main(String[] args) {
        System.out.println("✅ Java 编译器版本: " + System.getProperty("java.specification.version"));
        System.out.println("✅ JDK 版本: " + System.getProperty("java.version"));
        System.out.println("✅ JRE 版本: " + System.getProperty("java.runtime.version"));

        // 检查是否使用 Java 17
        if (System.getProperty("java.specification.version").equals("17")) {
            System.out.println("\n 恭喜！项目已成功配置为 Java 17");
        } else {
            System.out.println("\n 项目未正确配置为 Java 17");
        }
    }
}
