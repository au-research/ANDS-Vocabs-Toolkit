/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.roles.db.entity;

/** Authentication Service Id. */
public enum AuthenticationServiceId {

    /** Built-in. */
    AUTHENTICATION_BUILT_IN,

    /** LDAP. */
    AUTHENTICATION_LDAP,

    /** Shibboleth; Rapid Connect. */
    AUTHENTICATION_SHIBBOLETH,

    /** Facebook. */
    AUTHENTICATION_SOCIAL_FACEBOOK,

    /** Google. */
    AUTHENTICATION_SOCIAL_GOOGLE,

    /** Twitter. */
    AUTHENTICATION_SOCIAL_TWITTER

}
