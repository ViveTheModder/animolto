package cmd;
//ANIMOLTO: Character Package class by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CharaPak {

	public static final int MAX_NUM_BONES = 71;
	public static final String[] COORD_NAMES = { "X", "Y", "Z" };
	private byte[] valBytes = new byte[4];
	private boolean isNeo, bigEndian;
	private RandomAccessFile pak;
	
	public CharaPak(File f) throws IOException {
		pak = new RandomAccessFile(f, "r");
	}
	
	public boolean isInBigEndian() {
		return bigEndian;
	}
	public boolean isValid() throws IOException {
		pak.seek(0);
		pak.read(valBytes);
		if (valBytes[0] != -6 && valBytes[0] != -4) bigEndian = true;
		int numContents = ValueHandler.getVal(valBytes, bigEndian);
		pak.seek(4 + numContents * 4);
		pak.read(valBytes);
		int fileSize = ValueHandler.getVal(valBytes, bigEndian);
		boolean hasCorrectNumContents = numContents == 250 || numContents == 252;
		if (numContents == 250) isNeo = true;
		if (hasCorrectNumContents && fileSize == pak.length()) return true;
		return false;
	}
	public float getCollisionX() throws IOException {
		int offset = isNeo ? 64 : 4;
		pak.seek(72);
		pak.read(valBytes);
		pak.seek(ValueHandler.getVal(valBytes, bigEndian) + offset);
		pak.read(valBytes);
		return ValueHandler.getValFloat(valBytes, bigEndian);
	}
	public float[] getPositions(int boneId) throws IOException {
		int initBonePos = 0;
		float[] positions = new float[3];
		pak.seek(12);
		pak.read(valBytes);
		int mdlAddr = ValueHandler.getVal(valBytes, bigEndian);
		if (!isNeo) {
			pak.seek(mdlAddr + 108);
			pak.read(valBytes);
			initBonePos = mdlAddr + ValueHandler.getVal(valBytes, bigEndian);
		}
		else initBonePos = mdlAddr + 80;
 		pak.seek(initBonePos);
		for (int boneCnt = 0; boneCnt < MAX_NUM_BONES; boneCnt++) {
			pak.read(valBytes);
			int boneSize = ValueHandler.getVal(valBytes, bigEndian);
			pak.seek(initBonePos + 10);
			pak.read(valBytes);
			int boneIdFromMdl = ValueHandler.getVal(valBytes, bigEndian);
			pak.seek(initBonePos);
			byte[] boneBytes = new byte[boneSize];
			pak.read(boneBytes);
			initBonePos += boneSize;
			if (boneIdFromMdl == boneId) {
				for (int posCnt = 0; posCnt < positions.length; posCnt++) {
					System.arraycopy(boneBytes, 48 + posCnt * 4, valBytes, 0, valBytes.length);
					positions[posCnt] = ValueHandler.getValFloat(valBytes, bigEndian);
				}
				break;
			}
		}
		return positions;
	}
}