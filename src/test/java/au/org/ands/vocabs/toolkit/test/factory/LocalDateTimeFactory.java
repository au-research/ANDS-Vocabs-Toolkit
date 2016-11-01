/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.factory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.meanbean.factories.basic.RandomFactoryBase;
import org.meanbean.lang.Factory;
import org.meanbean.util.RandomValueGenerator;

/** Factory class for Mean Bean that generates instances of
 * {@link LocalDateTime}.
 */
public class LocalDateTimeFactory extends RandomFactoryBase<LocalDateTime>
    implements Factory<LocalDateTime> {

    /** Generated UID for serialization. */
    private static final long serialVersionUID = -2545128301226985988L;

    /** Modulus used in calculation of a random number of seconds
     * since epoch. Can't use an arbitrary long value, as these
     * can be too big for a LocalDateTime. */
    private static final long EPOCH_SECONDS_MODULUS = 4000000000L;

    /** Modulus used in calculation of a random number of nanoseconds.
     * Should be relatively prime to {@link EPOCH_SECONDS_MODULUS}
     * to make the calculation "interesting". */
    private static final int EPOCH_NANO_MODULUS = 987654;

    /** Construct a new LocalDateTime object factory.
     * @param randomValueGenerator A random value generator used by the
     *      Factory to generate random values.
     * @throws IllegalArgumentException If the specified randomValueGenerator
     *      is invalid, e.g., if it is null.
     */
    public LocalDateTimeFactory(final RandomValueGenerator randomValueGenerator)
            throws IllegalArgumentException {
            super(randomValueGenerator);
    }

    /** Create a new LocalDateTime object.
     * @return A new LocalDateTime object.
     */
    @Override
    public final LocalDateTime create() {
        // Get random time since the epoch.
        long randomTime = Math.abs(getRandomValueGenerator().nextLong());
        return LocalDateTime.ofEpochSecond(
                randomTime % EPOCH_SECONDS_MODULUS,
                (int) (randomTime % EPOCH_NANO_MODULUS),
                ZoneOffset.UTC);
    }

}
