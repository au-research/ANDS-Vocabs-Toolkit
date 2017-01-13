/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.schema.utils;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/** Plugin for xjc, that makes all generated fields private (rather
 * than protected).
 * Based on an answer given at:
 * <a
 *   href="http://stackoverflow.com/questions/9377923/why-do-jaxb-generated-classes-have-protected-members-and-how-can-i-change-this">
 * Stack Overflow: Why do JAXB generated classes have protected members
 * and how can I change this?</a>
 * Included here under the licence as stated at that page:
 * "Feel free to use this if you want."
 */
public class JaxbPrivateMemberPlugin extends Plugin {

    @Override
    public final String getOptionName() {
        return "Xpf";
    }

    @Override
    public final String getUsage() {
        return "  -Xpf               : set field visibility to private";
    }

    @Override
    public final boolean run(final Outline model, final Options opt,
            final ErrorHandler errorHandler) throws SAXException {
        for (ClassOutline co : model.getClasses()) {

            JDefinedClass jdc = co.implClass;
            // Avoid concurrent modification, by copying the fields into
            // a new list.
            List<JFieldVar> fields = new ArrayList<>(
                    jdc.fields().values());
            for (JFieldVar field : fields) {
                // Never touch the serialVersionUID field, if it exists.
                if (!field.name().equalsIgnoreCase("serialVersionuid")) {
                    // only try to change members that are not private
                    if (field.mods().getValue() != JMod.PRIVATE) {
                        field.mods().setPrivate();
                    }
                }
            }
        }
        return true;
    }

}
