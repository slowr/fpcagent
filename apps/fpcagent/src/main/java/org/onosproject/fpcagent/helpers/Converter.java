package org.onosproject.fpcagent.helpers;

import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.fpcidentity.FpcIdentityUnion;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Function;

public class Converter {
    /**
     * Short to Byte
     *
     * @param value - Short
     * @return byte value
     */
    public static byte toUint8(Short value) {
        return value.byteValue();
    }

    /**
     * Short to byte array
     *
     * @param value - Short
     * @return byte array
     */
    public static byte[] toUint16(Short value) {
        return new byte[]{(byte) (value >>> 8), (byte) (value & 0xFF)};
    }

    /**
     * Lower two bytes of an integer to byte array
     *
     * @param value - integer value
     * @return byte array
     */
    public static byte[] toUint16(Integer value) {
        return new byte[]{(byte) (value >>> 8), (byte) (value & 0xFF)};
    }

    /**
     * Long to byte array.
     *
     * @param value - long
     * @return byte array
     */
    public static byte[] toUint32(long value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) (value & 0xFF)};
    }

    /**
     * BigInteger to byte array.
     *
     * @param value - BigInteger
     * @return byte array
     */
    public static byte[] toUint64(BigInteger value) {
        return new byte[]{value.shiftRight(56).byteValue(), value.shiftRight(48).byteValue(), value.shiftRight(40).byteValue(),
                value.shiftRight(32).byteValue(), value.shiftRight(24).byteValue(), value.shiftRight(16).byteValue(),
                value.shiftRight(8).byteValue(), value.and(BigInteger.valueOf(0xFF)).byteValue()};
    }

    /**
     * Decodes a 32 bit value
     *
     * @param source - byte array
     * @param offset - offset in the array where the 8 bytes begins
     * @return integer
     */
    public static int toInt(byte[] source, int offset) {
        return new BigInteger(Arrays.copyOfRange(source, offset, offset + 4)).intValue();
    }

    /**
     * Converts a byte array to BigInteger
     *
     * @param source - byte array
     * @param offset - offset in the array where the 8 bytes begins
     * @return BigInteger representing a Uint64
     */
    public static BigInteger toBigInt(byte[] source, int offset) {
        return new BigInteger(Arrays.copyOfRange(source, offset, offset + 8));
    }

    /**
     * Converts an integer to a long (used for larger unsigned integers)
     *
     * @param source - message buffer (byte array)
     * @param offset - offset in the array where the 4 bytes begins
     * @return Long value of the unsigned integer
     */
    public static long fromIntToLong(byte[] source, int offset) {
        long value = 0;
        for (int i = offset; i < offset + 4; i++) {
            value = (value << 8) + (source[i] & 0xff);
        }
        return value;
    }

    public static Function<String, FpcIdentity> getFpcIdentity = (v) -> new FpcIdentity(new FpcIdentityUnion(v));
    public static Function<String, ClientIdentifier> getClientIdentity = (v) -> new ClientIdentifier(getFpcIdentity.apply(v));

}
