package com.visybl.visyblsdk_android;

import java.nio.ByteBuffer;

public class ConvertData {

    public static int convertU16ToInt(byte i) {
        int firstByte = (0x000000FF & ((int)i));
        return firstByte;
    }

    public static int bytesToInt(final byte[] array, final int start)
    {
        final ByteBuffer buf = ByteBuffer.wrap(array); // big endian by default
        buf.position(start);
        buf.put(array);
        buf.position(start);
        return buf.getInt();
    }

    public static int convertU32ToInt(byte b[], int start) {
        return ((b[start] << 24) & 0xff000000 |(b[start + 1] << 16) & 0xff0000
                | (b[start + 2] << 8) & 0xff00 | (b[start + 3]) & 0xff);
    }
    public static long int64Converter(byte buf[], int start) {
        return ((buf[start] & 0xFFL) << 56) | ((buf[start + 1] & 0xFFL) << 48)
                | ((buf[start + 2] & 0xFFL) << 40)
                | ((buf[start + 3] & 0xFFL) << 32)
                | ((buf[start + 4] & 0xFFL) << 24)
                | ((buf[start + 5] & 0xFFL) << 16)
                | ((buf[start + 6] & 0xFFL) << 8)
                | ((buf[start + 7] & 0xFFL) << 0);
    }

    public static long convertU16ToInt(byte[] buf, int index) {

        int firstByte  = (0x000000FF & ((int)buf[index]));
        int secondByte = (0x000000FF & ((int)buf[index+1]));
        int thirdByte  = (0x000000FF & ((int)buf[index+2]));
        int fourthByte = (0x000000FF & ((int)buf[index+3]));

        index = index+4;

        long anUnsignedInt  = ((long) (firstByte << 24
                | secondByte << 16
                | thirdByte << 8
                | fourthByte))
                & 0xFFFFFFFFL;

        return anUnsignedInt;
    }

    public static short toUnsigned(byte b) {
        return (short)(b & 0xff);
    }


    public static int convertU16ToInt(byte byte1, byte byte2) {
        int N = (( 255 - byte1 & 0xff )  << 8 ) | byte2 & 0xff;
        return N;
    }

    public static short UInt16Decode(byte inbyByteA, byte inbyByteB) {
        short n =  (short)(((inbyByteA & 0xFF) << 8) | (inbyByteB & 0xFF));
        return n;
    }


    public static long UInt32Decode(int inbyByteA, int inbyByteB) {
        int n = inbyByteA<< 16 | inbyByteB;

        return n;
    }


    public static long decodeMeasurement16(byte byte3, byte byte4) {
        return 0L;
    }

    public static double decodeMeasurement32(byte byte3, byte byte4, byte byte6, byte byte7) {

        double outdblFloatValue = 0;
        int outi16DecimalPointPosition = 0;

        int ui16Integer1 = convertU16ToInt (byte3, byte4);
        int ui16Integer2 = convertU16ToInt (byte6, byte7);

        int ui32Integer = ( (int)UInt32Decode (ui16Integer1, ui16Integer2) ) & 0x07FFFFFF;

        outi16DecimalPointPosition = ((0x000000FF - byte3 ) >> 3) - 15;

        // Decode raw value, with Exampledata: 0x05FFFFFC
        if ((100000000 + 0x2000000) > ui32Integer) {
            // Data is a valid value
            if (0x04000000 == (ui32Integer & 0x04000000)) {
                ui32Integer = (ui32Integer | 0xF8000000);
                // With Exampledata: 0xFDFFFFFC
            }
            ui32Integer = ui32Integer + 0x02000000; // with Exampledata: 0xFFFFFFFC
        }
        else {
            // Data contains error code, decode error code
            outdblFloatValue = (double)((ui32Integer - 0x02000000) - 16352.0);
            outi16DecimalPointPosition = 0;
            return -36; // Return value is error code
        }
        outdblFloatValue = (double)ui32Integer;
        outdblFloatValue = outdblFloatValue / (Math.pow(10.0f, (double)outi16DecimalPointPosition));

        return outdblFloatValue;
    }

    public static int toByte(int number) {
        int tmp = number & 0xff;
        return (tmp & 0x80) == 0 ? tmp : tmp - 256;
    }

    public static long getUnsignedInt(int x) {
        return x & 0x00000000ffffffffL;
    }
}
