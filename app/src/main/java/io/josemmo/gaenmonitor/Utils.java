package io.josemmo.gaenmonitor;

public class Utils {
    /**
     * String to split byte array
     * @param  data Input segments separated by "."
     * @return      Split byte array
     */
    public static byte[] toSplitByteArray(String data) {
        String[] segments = data.split("\\.");
        byte[] res = new byte[segments.length];
        for (int i=0; i<segments.length; i++) {
            res[i] = Byte.parseByte(segments[i]);
        }
        return res;
    }


    /**
     * To hex string
     * @param  bytes Byte array
     * @return       Hex string
     */
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
