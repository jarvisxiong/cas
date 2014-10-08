/**
 * 
 */
package com.inmobi.adserve.channels.server.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/**
 * @author ritwik.kumar
 * 
 */
public class JarVersionUtil {
  /**
   * 
   */
  private static Map<String, String> manifestMap;

  /**
   * 
   * @return
   */
  public static Map<String, String> getManifestData() {
    if (manifestMap == null || manifestMap.isEmpty()) {
      readManifestToMap();
    }
    return new HashMap<>(manifestMap);
  }

  /**
   * 
   * @return
   */
  private static void readManifestToMap() {
    manifestMap = new HashMap<>();
    try {
      final Class<JarVersionUtil> clazz = JarVersionUtil.class;
      final String className = clazz.getSimpleName() + ".class";
      final String classPath = clazz.getResource(className).toString();
      if (classPath.startsWith("jar")) {
        // Class from JAR
        final String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        final Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        final Attributes attr = manifest.getMainAttributes();
        for (final Entry<Object, Object> entry : attr.entrySet()) {
          manifestMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
      } else {
        // Class not from JAR
        manifestMap.put("ERROR", "Class not from JAR");
      }

    } catch (final Exception e) {
      manifestMap.put("ERROR", e.getMessage());
    }
  }



  /**
   * @param args
   */
  public static void main(final String[] args) throws Exception {
    System.out.println(getManifestData());
  }

}
