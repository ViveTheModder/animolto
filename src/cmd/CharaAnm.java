package cmd;
//ANIMOLTO: Character Animation class by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CharaAnm {
	private float[][] coordinates;
	private int[][] positions;
	private RandomAccessFile anm;
	
	public CharaAnm(File f) throws IOException {
		anm = new RandomAccessFile(f, "rw");
	}
	
	public boolean isValid() throws IOException {
		anm.seek(0);
		byte headerVal = anm.readByte();
		byte[] offsetBytes = new byte[2];
		if (headerVal == 97) {
			anm.seek(4);
			anm.read(offsetBytes);
			int offsetAtPos4 = ValueHandler.getVal(offsetBytes, false);
			anm.seek(8);
			anm.read(offsetBytes);
			int offsetAtPos8 = ValueHandler.getVal(offsetBytes, false);
			if (offsetAtPos4 == 0 && offsetAtPos8 == 0) return true;
		}
		return false;
	}
	public int[] getTranslationBoneIds() throws IOException {
		byte[] offsetBytes = new byte[2];
		int transBoneCnt = 0;
		int[] transBoneIds = new int[CharaPak.MAX_NUM_BONES];
		for (int boneCnt = 0; boneCnt < CharaPak.MAX_NUM_BONES; boneCnt++) {
			anm.seek(6 + boneCnt * 2);
			anm.read(offsetBytes);
			int bonePos = ValueHandler.getVal(offsetBytes, false) * 4;
			if (bonePos == 0) continue;
			anm.seek(bonePos);
			anm.read(offsetBytes);
			int movementType = ValueHandler.getVal(offsetBytes, false);
			if (movementType == 0) {
				transBoneIds[transBoneCnt] = boneCnt;
				transBoneCnt++;
			}
		}
		return transBoneIds;
	}
	public String writeNewCoordinates(float coefficient, float[] srcBoneCoords, float[] dstBoneCoords, String boneName, int boneId) throws IOException {
		String output = "";
		coordinates = getCoordinates(boneId);
		if (coordinates == null) return output;
		else {
			if (boneName == null) boneName = "Unknown";
			output += "* Working on " + boneName + " (bone " + boneId + ")..." + "\n";
			for (int posCnt = 0; posCnt < positions.length; posCnt++) {	
				for (int coordCnt = 0; coordCnt < positions[0].length; coordCnt++) {
					anm.seek(positions[posCnt][coordCnt]);
					float newVal = coordinates[posCnt][coordCnt] - dstBoneCoords[coordCnt];
					if (coordCnt == 1) newVal *= coefficient;
					newVal += srcBoneCoords[coordCnt];
					output += "-> Writing new " + CharaPak.COORD_NAMES[coordCnt] + " value at pos. " + positions[posCnt][coordCnt];
					output += String.format(" (%6s: %.6f%7s: %.6f)\n", "BEFORE", coordinates[posCnt][coordCnt], ", AFTER", newVal);
					anm.write(ValueHandler.getValBytes(newVal, false));
				}
			}
		}
		anm.close();
		return output;
	}
	
	private float[][] getCoordinates(int boneId) throws IOException {
		byte[] offsetBytes = new byte[2];
		for (int boneCnt = 0; boneCnt < CharaPak.MAX_NUM_BONES; boneCnt++) {
			anm.seek(6 + boneCnt * 2);
			anm.read(offsetBytes);
			int bonePos = ValueHandler.getVal(offsetBytes, false) * 4;
			if (bonePos == 0) continue;
			anm.seek(bonePos);
			anm.read(offsetBytes);
			int movementType = ValueHandler.getVal(offsetBytes, false);
			if (movementType == 0) {
				anm.read(offsetBytes);
				int numKeyFrames = ValueHandler.getVal(offsetBytes, false);
				positions = new int[numKeyFrames][3];
				anm.seek(bonePos);
				int boneSize = 4 + 24 * numKeyFrames;
				byte[] boneBytes = new byte[boneSize];
				anm.read(boneBytes);
				if (boneCnt == boneId) {
					float[][] coordinates = new float[numKeyFrames][3];
					byte[] positionBytes = new byte[4];
					for (int keyFrameCnt = 0; keyFrameCnt < numKeyFrames; keyFrameCnt++) {
						for (int posCnt = 0; posCnt < 3; posCnt++) {
							int addr = 4 + 24 * keyFrameCnt + 4 * posCnt;
							System.arraycopy(boneBytes, addr, positionBytes, 0, positionBytes.length);
							coordinates[keyFrameCnt][posCnt] = ValueHandler.getValFloat(positionBytes, false);
							positions[keyFrameCnt][posCnt] = bonePos + addr;
						}
					}
					return coordinates;
				}
			}
		}
		return null;
	}
}