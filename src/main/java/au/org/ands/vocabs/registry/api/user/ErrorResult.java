/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.api.user;

import javax.xml.bind.annotation.XmlRootElement;

/** Class for representing an error result for an API call. */
@SuppressWarnings("checkstyle:DesignForExtension")
@XmlRootElement(name = "error")
public class ErrorResult {

    /** The text of the error message. */
    private String message;

    /** Default constructor.
     */
    public ErrorResult() {
    }

    /** Constructor that takes the text of the error message as a parameter.
     * @param aMessage The text of the error message.
     */
    public ErrorResult(final String aMessage) {
        message = aMessage;
    }

    /** Set the text of error message.
     * @param aMessage The text of the error message.
     */
    public void setMessage(final String aMessage) {
        message = aMessage;
    }

    /** Get the text of the error message.
     * @return The text of the error message.
     */
    public String getMessage() {
        return message;
    }

}
