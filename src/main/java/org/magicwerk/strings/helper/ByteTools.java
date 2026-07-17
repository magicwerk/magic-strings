/*
 * Copyright 2010 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.magicwerk.strings.helper;

/**
 * Class {@link ByteTools} offers methods for converting byte, short, and int values
 * into a byte array and back. To support both big and little endian 
 * value representation, there are always to methods which differ just
 * by the trailing character 'B' or 'L'. There is also support for working
 * with unsigned values which is not supported out of the box by Java.
 * Consider using a java.nio.ByteBuffer which offers a lot of functionality
 * out of the box including support for big and little endian values.
 */
public class ByteTools {

	public static int byteToUbyte(byte value) {
		return value & 0xff;
	}

	public static byte ubyteToByte(int value) {
		return (byte) value;
	}

	public static int shortToUshort(short value) {
		return value & 0xffff;
	}

	public static short ushortToShort(int value) {
		return (short) value;
	}

	// Byte

	public static byte[] byteToByteArray(byte value) {
		return new byte[] { value };
	}

	public static byte byteArrayToByte(byte[] b) {
		return b[0];
	}

	// Unsigned Byte

	public static byte[] ubyteToByteArray(short value) {
		return new byte[] { (byte) value };
	}

	public static short byteArrayToUByte(byte[] b) {
		return (short) (b[0] & 0xff);
	}

	// Short

	public static byte[] shortToByteArrayB(short value) {
		return new byte[] {
				(byte) (value >>> 8), (byte) (value)
		};
	}

	public static short byteArrayToShortB(byte[] buf) {
		return bytesToShort(buf[0], buf[1]);
	}

	public static byte[] shortToByteArrayL(short value) {
		return new byte[] {
				(byte) (value), (byte) (value >>> 8)
		};
	}

	public static short byteArrayToShortL(byte[] buf) {
		return bytesToShort(buf[1], buf[0]);
	}

	public static short bytesToShort(byte b0, byte b1) {
		return (short) (((b0 & 0xff) << 8) | (b1 & 0xff));
	}

	// Unsigned Short

	public static byte[] ushortToByteArrayB(int value) {
		return new byte[] {
				(byte) (value >>> 8), (byte) (value)
		};
	}

	public static int byteArrayToUShortB(byte[] b) {
		return ((b[0] & 0xff) << 8) | (b[1] & 0xff);
	}

	public static byte[] ushortToByteArrayL(int value) {
		return new byte[] {
				(byte) (value), (byte) (value >>> 8)
		};
	}

	public static int byteArrayToUShortL(byte[] b) {
		return ((b[1] & 0xff) << 8) | (b[0] & 0xff);
	}

	public static int bytesToUShort(byte b0, byte b1) {
		return ((b0 & 0xff) << 8) | (b1 & 0xff);
	}

	// Int

	public static byte[] intToByteArrayB(int value) {
		return new byte[] {
				(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) (value)
		};
	}

	public static int byteArrayToIntB(byte[] buf) {
		return bytesToInt(buf[0], buf[1], buf[2], buf[3]);
	}

	public static byte[] intToByteArrayL(int value) {
		return new byte[] {
				(byte) (value), (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24)
		};
	}

	public static int byteArrayToIntL(byte[] buf) {
		return bytesToInt(buf[3], buf[2], buf[1], buf[0]);
	}

	public static int bytesToInt(byte b0, byte b1, byte b2, byte b3) {
		return (b0 << 24) | ((b1 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
	}

	// Int with buffer

	public static void intToByteArrayB(int value, byte[] buf, int offset) {
		buf[offset + 0] = (byte) (value >>> 24);
		buf[offset + 1] = (byte) (value >>> 16);
		buf[offset + 2] = (byte) (value >>> 8);
		buf[offset + 3] = (byte) (value);
	}

	public static int byteArrayToIntB(byte[] buf, int offset) {
		return bytesToInt(buf[offset + 0], buf[offset + 1], buf[offset + 2], buf[offset + 3]);
	}

	public static void intToByteArrayL(int value, byte[] buf, int offset) {
		buf[offset + 0] = (byte) (value);
		buf[offset + 1] = (byte) (value >>> 8);
		buf[offset + 2] = (byte) (value >>> 16);
		buf[offset + 3] = (byte) (value >>> 24);
	}

	public static int byteArrayToIntL(byte[] buf, int offset) {
		return bytesToInt(buf[offset + 3], buf[offset + 2], buf[offset + 1], buf[offset + 0]);
	}

	// Unsigned Int

	public static byte[] uintToByteArrayB(long value) {
		return new byte[] {
				(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) (value)
		};
	}

	public static long byteArrayToUIntB(byte[] buf) {
		return bytesToUInt(buf[0], buf[1], buf[2], buf[3]);
	}

	public static byte[] uintToByteArrayL(long value) {
		return new byte[] {
				(byte) (value), (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24)
		};
	}

	public static long byteArrayToUIntL(byte[] buf) {
		return bytesToUInt(buf[3], buf[2], buf[1], buf[0]);
	}

	public static long bytesToUInt(byte b0, byte b1, byte b2, byte b3) {
		return ((long) (b0 & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff);
	}

	// Long

	public static byte[] longToByteArrayB(long value) {
		return new byte[] {
				(byte) (value >>> 56), (byte) (value >>> 48), (byte) (value >>> 40), (byte) (value >>> 32),
				(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) (value)
		};
	}

	public static long byteArrayToLongB(byte[] buf) {
		return bytesToLong(buf[0], buf[1], buf[2], buf[3], buf[4], buf[5], buf[6], buf[7]);
	}

	public static byte[] longToByteArrayL(long value) {
		return new byte[] {
				(byte) (value), (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24),
				(byte) (value >>> 32), (byte) (value >>> 40), (byte) (value >>> 48), (byte) (value >>> 56)
		};
	}

	public static long byteArrayToLongL(byte[] buf) {
		return bytesToLong(buf[7], buf[6], buf[5], buf[4], buf[3], buf[2], buf[1], buf[0]);
	}

	public static long bytesToLong(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
		return ((long) b0 << 56) | (((long) b1 & 0xFF) << 48) | (((long) b2 & 0xFF) << 40) | (((long) b3 & 0xFF) << 32) |
				(((long) b4 & 0xFF) << 24) | (((long) b5 & 0xFF) << 16) | (((long) b6 & 0xFF) << 8) | ((long) b7 & 0xFF);
	}

	// Long with buffer

	public static void longToByteArrayB(long value, byte[] buf, int offset) {
		buf[offset + 0] = (byte) (value >>> 56);
		buf[offset + 1] = (byte) (value >>> 48);
		buf[offset + 2] = (byte) (value >>> 40);
		buf[offset + 3] = (byte) (value >>> 32);
		buf[offset + 4] = (byte) (value >>> 24);
		buf[offset + 5] = (byte) (value >>> 16);
		buf[offset + 6] = (byte) (value >>> 8);
		buf[offset + 7] = (byte) (value);
	}

	public static long byteArrayToLongB(byte[] buf, int offset) {
		return bytesToLong(buf[offset + 0], buf[offset + 1], buf[offset + 2], buf[offset + 3],
				buf[offset + 4], buf[offset + 5], buf[offset + 6], buf[offset + 7]);
	}

	public static void longToByteArrayL(long value, byte[] buf, int offset) {
		buf[offset + 0] = (byte) (value);
		buf[offset + 1] = (byte) (value >>> 8);
		buf[offset + 2] = (byte) (value >>> 16);
		buf[offset + 3] = (byte) (value >>> 24);
		buf[offset + 4] = (byte) (value >>> 32);
		buf[offset + 5] = (byte) (value >>> 40);
		buf[offset + 6] = (byte) (value >>> 48);
		buf[offset + 7] = (byte) (value >>> 56);
	}

	public static long byteArrayToLongL(byte[] buf, int offset) {
		return bytesToLong(buf[offset + 7], buf[offset + 6], buf[offset + 5], buf[offset + 4],
				buf[offset + 3], buf[offset + 2], buf[offset + 1], buf[offset + 0]);
	}

	// Float
	// putIntB(a, Float.floatToRawIntBits(x));

	// Double
	// putLongB(bb, bi, Double.doubleToRawLongBits(x));

}
