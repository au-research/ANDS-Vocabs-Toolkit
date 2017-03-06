/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.api.user;

import javax.xml.bind.annotation.XmlRootElement;

/** Class for representing a simple result from an API call.
 * Either, use the no-args constructor, then call <i>one</i> setter,
 * or use one of the one-arg constructors, and call <i>no</i> setter. */
@SuppressWarnings("checkstyle:DesignForExtension")
@XmlRootElement(name = "result")
public class SimpleResult {

    /** Default constructor.
     */
    public SimpleResult() {
    }

    /** The String value result. */
    private String stringValue;

    /** Constructor that takes a String value as a parameter.
     * @param aResult The result as a String value.
     */
    public SimpleResult(final String aResult) {
        stringValue = aResult;
    }

    /** Set the result as a String value.
     * @param aStringValue The result as a String value.
     */
    public void setStringValue(final String aStringValue) {
        stringValue = aStringValue;
    }

    /** Get the String value.
     * @return The text of the String value.
     */
    public String getStringValue() {
        return stringValue;
    }

    /** The Boolean value result. */
    private Boolean booleanValue;

    /** Constructor that takes a Boolean value as a parameter.
     * @param aResult The result as a Boolean value.
     */
    public SimpleResult(final Boolean aResult) {
        booleanValue = aResult;
    }

    /** Set the text of error message.
     * @param aBooleanValue The result as a Boolean value.
     */
    public void setBooleanValue(final Boolean aBooleanValue) {
        booleanValue = aBooleanValue;
    }

    /** Get the String value.
     * @return The text of the String value.
     */
    public Boolean getBooleanValue() {
        return booleanValue;
    }

}
