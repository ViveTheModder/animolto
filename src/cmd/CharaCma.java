package cmd;
//ANIMOLTO: Character Camera class by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class CharaCma {
	private static final byte[] CMAN_HEADER = { 0x43, 0x4D, 0x41, 0x6E };
	private boolean bigEndian;
	private int axisIndexDataAddr, numAxes;
	private RandomAccessFile cma;
	
	public CharaCma(File f, boolean bigEndian) throws IOException {
		byte[] input = new byte[4];
		cma = new RandomAccessFile(f, "rw");
		this.bigEndian = bigEndian;
		cma.seek(4);
		cma.read(input);
		numAxes = ValueHandler.getVal(input, bigEndian);
		cma.read(input);
		axisIndexDataAddr = ValueHandler.getVal(input, bigEndian);
	}
	
	public boolean isValid() throws IOException {
		//Replace 'n' with 'w' (CMAn -> CMAw)
		if (bigEndian) CMAN_HEADER[3] = 0x77;
		byte[] input = new byte[4];
		cma.seek(0);
		cma.read(input);
		if (Arrays.equals(input, CMAN_HEADER)) {
			cma.seek(12);
			cma.read(input);
			int numAxes2 = ValueHandler.getVal(input, bigEndian);
			if (numAxes == numAxes2 && axisIndexDataAddr == 80) {
				cma.seek(28);
				cma.read(input);
				int fileSizeNoHeader = ValueHandler.getVal(input, bigEndian);
				if (fileSizeNoHeader <= (int) cma.length() - 16) return true;
			}
		}
		return false;
	}
	public String writeNewOrdinates(float coefficient) throws IOException {
		String output = "";
		byte[] axisIndexData = new byte[16], buffer = new byte[4];
		//The axis parameters are as follows: axis ID, axis start offset, number of axis points
		int[] axisParams = new int[3];
		cma.seek(axisIndexDataAddr);
		for (int axisCnt = 0; axisCnt < numAxes; axisCnt++) {
			cma.read(axisIndexData);
			for (int paramCnt = 0; paramCnt < axisParams.length; paramCnt++) {
				System.arraycopy(axisIndexData, paramCnt * buffer.length, buffer, 0, buffer.length);
				axisParams[paramCnt] = ValueHandler.getVal(buffer, bigEndian);
			}
			//Check if the axis ID belongs to the Y-Position axis
			if (axisParams[0] == 1) {
				float[] axisValues = new float[axisParams[2]];
				for (int floatCnt = 0; floatCnt < axisValues.length; floatCnt++) {
					int floatPos = axisParams[1] + (floatCnt * 32) + 8;
					cma.seek(floatPos);
					cma.read(buffer);
					axisValues[floatCnt] = ValueHandler.getValFloat(buffer, bigEndian);
					output += "* Writing new ordinate at pos. " + floatPos;
					output += String.format(" (%6s: %.6f", "BEFORE", axisValues[floatCnt], ", AFTER", axisValues[floatCnt]);
					axisValues[floatCnt] *= coefficient;
					output += String.format("%7s: %.6f)\n", ", AFTER", axisValues[floatCnt]);
					cma.seek(floatPos);
					cma.write(ValueHandler.getValBytes(axisValues[floatCnt], bigEndian));
				}
				break;
			}
		}
		cma.close();
		return output;
	}
}