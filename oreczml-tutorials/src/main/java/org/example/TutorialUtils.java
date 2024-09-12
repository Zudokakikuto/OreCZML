package org.example;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;

import java.io.File;

public class TutorialUtils {
   public static void loadOrekitData() {
      try {
         final File         home      = new File(System.getProperty("user.home"));
         final File         orekitDir = new File(home, "orekit-data");
         final DataProvider provider  = new DirectoryCrawler(orekitDir);
         DataContext.getDefault()
                    .getDataProvidersManager()
                    .addProvider(provider);
      }
      catch (OrekitException oe) {
         System.err.println(oe.getLocalizedMessage());
      }
   }

   public static String loadResources(final String resourcePath) {
      return TutorialUtils.class.getClassLoader().getResource(resourcePath).getPath().toString();
   }
}
