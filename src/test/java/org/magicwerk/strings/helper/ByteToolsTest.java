package org.magicwerk.strings.helper;

import org.magictest.client.Format;
import org.magictest.client.Formatter;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.strings.escape.StringEscapeTools;
import org.magicwerk.strings.helper.ByteTools;

/**
 * Test of class {@link ByteTools}.
 */
public class ByteToolsTest {

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testMisc();
	}

	static void testMisc() {
		byte value = -10;

		int i1 = value;
		int i2 = value & 0xff;
		System.out.println(i1 + " - " + i2);

		byte b1 = (byte) i1;
		System.out.println(b1 + " - ");
	}

	@Trace
	public static void testByteToUbyte() {
		ByteTools.byteToUbyte((byte) -1);
		ByteTools.byteToUbyte((byte) 0);
		ByteTools.byteToUbyte((byte) 127);
		ByteTools.byteToUbyte((byte) -128);
	}

	// --- Byte ---

	@Trace(formats = { @Format(apply = Trace.RESULT, formatter = "formatByteArray") })
	public static void testByteToByteArray() {
		ByteTools.byteToByteArray((byte) 127);
		ByteTools.byteToByteArray((byte) -128);
		ByteTools.byteToByteArray((byte) 0);
	}

	@Trace(formats = { @Format(apply = Trace.PARAM0, formatter = "formatByteArray") })
	public static void testByteArrayToByte() {
		ByteTools.byteArrayToByte(new byte[] { 127 });
		ByteTools.byteArrayToByte(new byte[] { -128 });
		ByteTools.byteArrayToByte(new byte[] { 0 });
	}

	// --- Unsigned Byte ---

	@Trace(formats = { @Format(apply = Trace.RESULT, formatter = "formatByteArray") })
	public static void testUbyteToByteArray() {
		ByteTools.ubyteToByteArray((short) 127);
		ByteTools.ubyteToByteArray((short) 128);
		ByteTools.ubyteToByteArray((short) 255);
		ByteTools.ubyteToByteArray((short) -1);
	}

	@Trace(formats = { @Format(apply = Trace.PARAM0, formatter = "formatByteArray") })
	public static void testByteArrayToUByte() {
		ByteTools.byteArrayToUByte(new byte[] { 0x7f });
		ByteTools.byteArrayToUByte(new byte[] { (byte) 0x80 });
		ByteTools.byteArrayToUByte(new byte[] { (byte) 0xff });
	}

	// --- Short ---

	@Trace(traceMethod = "/shortToByteArray[BL]/", formats = { @Format(apply = Trace.RESULT, formatter = "formatByteArray") })
	public static void testShortToByteArray() {
		ByteTools.shortToByteArrayB((short) 0x1234);
		ByteTools.shortToByteArrayL((short) 0x1234);
		ByteTools.shortToByteArrayB((short) 0x8421);
		ByteTools.shortToByteArrayL((short) 0x8421);
		ByteTools.shortToByteArrayB((short) 0);
		ByteTools.shortToByteArrayL((short) 0);
	}

	@Trace(traceMethod = "/byteArrayToShort[BL]/", formats = { @Format(apply = Trace.PARAM0, formatter = "formatByteArray") })
	public static void testByteArrayToShort() {
		ByteTools.byteArrayToShortB(new byte[] { 0x12, 0x34 });
		ByteTools.byteArrayToShortL(new byte[] { 0x34, 0x12 });
		ByteTools.byteArrayToShortB(new byte[] { (byte) 0x84, (byte) 0x21 });
		ByteTools.byteArrayToShortL(new byte[] { (byte) 0x21, (byte) 0x84 });
		ByteTools.byteArrayToShortB(new byte[] { 0x00, 0x00 });
		ByteTools.byteArrayToShortL(new byte[] { 0x00, 0x00 });
	}

	// --- Unsigned Short ---

	@Trace(traceMethod = "/ushortToByteArray[BL]/", formats = { @Format(apply = Trace.RESULT, formatter = "formatByteArray") })
	public static void testUShortToByteArray() {
		ByteTools.ushortToByteArrayB(0x1234);
		ByteTools.ushortToByteArrayL(0x1234);
		ByteTools.ushortToByteArrayB(0x8421);
		ByteTools.ushortToByteArrayL(0x8421);
		ByteTools.ushortToByteArrayB(0);
		ByteTools.ushortToByteArrayL(0);
	}

	@Trace(traceMethod = "/byteArrayToUShort[BL]/", formats = { @Format(apply = Trace.PARAM0, formatter = "formatByteArray") })
	public static void testByteArrayToUShort() {
		ByteTools.byteArrayToUShortB(new byte[] { 0x12, 0x34 });
		ByteTools.byteArrayToUShortL(new byte[] { 0x34, 0x12 });
		ByteTools.byteArrayToUShortB(new byte[] { (byte) 0x84, (byte) 0x21 });
		ByteTools.byteArrayToUShortL(new byte[] { (byte) 0x21, (byte) 0x84 });
		ByteTools.byteArrayToUShortB(new byte[] { 0x00, 0x00 });
		ByteTools.byteArrayToUShortL(new byte[] { 0x00, 0x00 });
	}

	// --- Int ---

	@Trace(traceMethod = "/intToByteArray[BL]/", formats = { @Format(apply = Trace.RESULT, formatter = "formatByteArray") })
	public static void testIntToByteArray() {
		ByteTools.intToByteArrayB(0x12345678);
		ByteTools.intToByteArrayL(0x12345678);
		ByteTools.intToByteArrayB(0x87654321);
		ByteTools.intToByteArrayL(0x87654321);
		ByteTools.intToByteArrayB(0);
		ByteTools.intToByteArrayL(0);
	}

	@Trace(traceMethod = "/byteArrayToInt[BL]/", formats = { @Format(apply = Trace.PARAM0, formatter = "formatByteArray") })
	public static void testByteArrayToInt() {
		ByteTools.byteArrayToIntB(new byte[] { 0x12, 0x34, 0x56, 0x78 });
		ByteTools.byteArrayToIntL(new byte[] { 0x78, 0x56, 0x34, 0x12 });
		ByteTools.byteArrayToIntB(new byte[] { (byte) 0x87, 0x65, 0x43, 0x21 });
		ByteTools.byteArrayToIntL(new byte[] { 0x21, 0x43, 0x65, (byte) 0x87 });
		ByteTools.byteArrayToIntB(new byte[] { 0x00, 0x00, 0x00, 0x00 });
		ByteTools.byteArrayToIntL(new byte[] { 0x00, 0x00, 0x00, 0x00 });
	}

	// --- Unsigned Int ---

	@Trace(traceMethod = "/uintToByteArray[BL]/", formats = { @Format(apply = Trace.RESULT, formatter = "formatByteArray") })
	public static void testUIntToByteArray() {
		ByteTools.uintToByteArrayB(0x12345678);
		ByteTools.uintToByteArrayL(0x12345678);
		ByteTools.uintToByteArrayB(0x87654321L);
		ByteTools.uintToByteArrayL(0x87654321L);
		ByteTools.uintToByteArrayB(0);
		ByteTools.uintToByteArrayL(0);
	}

	@Trace(traceMethod = "/byteArrayToUInt[BL]/", formats = { @Format(apply = Trace.PARAM0, formatter = "formatByteArray") })
	public static void testByteArrayToUInt() {
		ByteTools.byteArrayToUIntB(new byte[] { 0x12, 0x34, 0x56, 0x78 });
		ByteTools.byteArrayToUIntL(new byte[] { 0x78, 0x56, 0x34, 0x12 });
		ByteTools.byteArrayToUIntB(new byte[] { (byte) 0x87, 0x65, 0x43, 0x21 });
		ByteTools.byteArrayToUIntL(new byte[] { 0x21, 0x43, 0x65, (byte) 0x87 });
		ByteTools.byteArrayToUIntB(new byte[] { 0x00, 0x00, 0x00, 0x00 });
		ByteTools.byteArrayToUIntL(new byte[] { 0x00, 0x00, 0x00, 0x00 });
	}

	// --- Formatter ---
	@Formatter
	public static String formatByteArray(byte[] data) {
		return StringEscapeTools.toHexString(data);
	}

}
