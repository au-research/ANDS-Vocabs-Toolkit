/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Test of parsing JSON. */
public final class TestJsonParsing {

    /** Private constructor for test class. */
    private TestJsonParsing() {
    }

    /** Main program.
     * @param args Command-line arguments.
     */
    public static void main(final String[] args) {
      String jsonString =
              "[{\"type\": \"HARVEST\",\"provider_type\":"
              + " \"PoolParty\",\"project_id\":"
              + " \"1DCE1494-A022-0001-FFBD-12DE19E01FEB\"},"
              + "{\"type\": \"TRANSFORM\"},{\"type\": \"IMPORT\"}]";
      JsonNode root = TaskUtils.jsonStringToTree(jsonString);
      if (root == null) {
          System.out.println("Got null.");
      } else {
          System.out.println("Got instance of:" + root.getClass().toString());
          if (!(root instanceof ArrayNode)) {
              System.out.println("Didn't get an array.");
          } else {
              for (JsonNode node : (ArrayNode) root) {
                  System.out.println("Got element: " + node.toString());
                  if (!(node instanceof ObjectNode)) {
                      System.out.println("Didn't get an object.");
                  } else {
                      System.out.println("task type: "
                              + node.get("type"));
                  }
              }
          }
      }
    }

}
