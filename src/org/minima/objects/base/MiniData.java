/**
 * 
 */
package org.minima.objects.base;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import org.minima.miniscript.Contract;
import org.minima.utils.BaseConverter;
import org.minima.utils.MinimaLogger;
import org.minima.utils.Streamable;

/**
 * @author Spartacus Rex
 *
 */
public class MiniData implements Streamable {

//	/**
//	 * Utility HEX conversion functions
//	 * 
//	 * @param zHex
//	 * @return
//	 */
//	protected static byte[] hexStringToByteArray(String zHex) {
//		String hex = zHex;
//		if(hex.startsWith("0x")) {
//			hex = zHex.substring(2);
//		}		
//		
//		//Go Upper case - make sure always the same
//		hex = hex.toUpperCase();
//		int len = hex.length();
//	
//		//Must be 2 digits per byte
//		if(len % 2 != 0) {
//			//Need a leading zero
//			hex="0"+hex;
//			len = hex.length();
//		}
//		
//		byte[] data = new byte[len / 2];
//	    for (int i = 0; i < len; i += 2) {
//	        data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i+1), 16));
//	    }
//	    
//	    return data;
//	}
//	
//	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
//	protected static String bytesToHex(byte[] bytes) {
//	    char[] hexChars = new char[bytes.length * 2];
//	    for ( int j = 0; j < bytes.length; j++ ) {
//	        int v = bytes[j] & 0xFF;
//	        hexChars[j * 2] = hexArray[v >>> 4];
//	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//	    }
//	    return new String(hexChars);
//	}
	
	/**
	 * The byte data
	 */
	protected byte[] mData;
	
	/**
	 * The numeric value of the data
	 */
	protected BigInteger mDataVal;
	
	public MiniData() {
		this("");
	}
	
	public MiniData(String zHexString) {
		this(BaseConverter.decode16(zHexString));
	}
	
	public MiniData(byte[] zData) {
		mData = zData;
		setDataValue();
	}
	
	public int getLength() {
		return mData.length;
	}
	
	public byte[] getData() {
		return mData;
	}
	
	protected void setDataValue() {
		mDataVal = new BigInteger(1,mData);
	}
	
	public BigInteger getDataValue() {
		return mDataVal;
	}
	
	public BigDecimal getDataValueDecimal() {
		return new BigDecimal(mDataVal);
	}
	
	@Override
	public boolean equals(Object o) {
		MiniData data = (MiniData)o;
		return isExactlyEqual(data);
	}
	
	public boolean isExactlyEqual(MiniData zCompare) {
		if(getLength() != zCompare.getLength()) {
			return false;
		}
		
		return mDataVal.compareTo(zCompare.getDataValue()) == 0;
	}
	
	public boolean isNumericallyEqual(MiniData zCompare) {
		return mDataVal.compareTo(zCompare.getDataValue()) == 0;
	}
	
	public boolean isLess(MiniData zCompare) {
		return mDataVal.compareTo(zCompare.getDataValue()) < 0;
	}
	
	public boolean isLessEqual(MiniData zCompare) {
		return mDataVal.compareTo(zCompare.getDataValue()) <= 0;
	}
	
	public boolean isMore(MiniData zCompare) {
		return mDataVal.compareTo(zCompare.getDataValue()) > 0;
	}
	
	public boolean isMoreEqual(MiniData zCompare) {
		return mDataVal.compareTo(zCompare.getDataValue()) >= 0;
	}
	
	public MiniData shiftr(int zNumber) {
		return new MiniData(mDataVal.shiftRight(zNumber).toString(16).toUpperCase());
	}
	
	public MiniData shiftl(int zNumber) {
		return new MiniData(mDataVal.shiftLeft(zNumber).toString(16).toUpperCase());
	}
	
	public int compare(MiniData zCompare) {
		return mDataVal.compareTo(zCompare.getDataValue());
	}
	
	public MiniData concat(MiniData zConcat) {
		int locallen  = getLength();
		int concatlen = zConcat.getData().length; 
				
		int totlen   = locallen+concatlen;
		byte[] total = new byte[totlen]; 
		
		//First copy local..
		System.arraycopy(getData(), 0, total, 0, locallen);
		
		//Then the new..
		System.arraycopy(zConcat.getData(), 0, total, locallen, concatlen);
		
		return new MiniData(total);
	}
	
	@Override
	public String toString() {
		return to0xString();
	}
	
	/**
	 * Remove the 0x at the beginning
	 * @return
	 */
	public String toPureHexString() {
		return toString().substring(2);
	}
	
	public String toShort0xString() {
		return toShort0xString(8);
	}
	
	public String toShort0xString(int zLen) {
		String data = to0xString();
		int len = data.length();
		if(len > zLen) {
			len = zLen;
		}
		return data.substring(0, len).concat("..");
	}

	public String to0xString() {
		return BaseConverter.encode16(mData);
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		zOut.writeInt(mData.length);
		zOut.write(mData);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		int len = zIn.readInt();
		mData = new byte[len];
		zIn.readFully(mData);
		
		//Set the data value
		setDataValue();
	}

	
	public static MiniData ReadFromStream(DataInputStream zIn){
		MiniData data = new MiniData();
		
		try {
			data.readDataStream(zIn);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return data;
	}

	/**
	 * Get a random chunk of data
	 * 
	 * @param len
	 * @return
	 */
	public static MiniData getRandomData(int len) {
		Random rand = new Random();
		byte[] data = new byte[len];
		rand.nextBytes(data);
		return new MiniData(data);
	}
	
	
	public static void main(String[] zArgs) {
		MiniData data = new MiniData("00000FFF");
		
		MinimaLogger.log("data    : "+data.toString());
		MinimaLogger.log("value   : "+data.getDataValue().toString());
	}
}
