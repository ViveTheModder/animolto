package cmd;
//ANIMOLTO: Character Package class by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CharaPak {

	public static final int MAX_NUM_BONES = 71;
	public static final String[] COORD_NAMES = { "X", "Y", "Z" };
	private byte[] valBytes = new byte[4];
	private RandomAccessFile pak;
	
	public CharaPak(File f) throws IOException {
		pak = new RandomAccessFile(f, "r");
	}
	
	public boolean isValid() throws IOException {
		pak.seek(0);
		pak.read(valBytes);
		int numContents = ValueHandler.getVal(valBytes, false);
		pak.seek(4 + numContents * 4);
		pak.read(valBytes);
		int fileSize = ValueHandler.getVal(valBytes, false);
		boolean hasCorrectNumContents = numContents == 250 || numContents == 252;
		if (hasCorrectNumContents && fileSize == pak.length()) return true;
		return false;
	}
	public float getCollisionX() throws IOException {
		pak.seek(72);
		pak.read(valBytes);
		pak.seek(ValueHandler.getVal(valBytes, false) + 4);
		pak.read(valBytes);
		return ValueHandler.getValFloat(valBytes, false);
	}
	public float[] getPositions(int boneId) throws IOException {
		float[] positions = new float[3];
		pak.seek(12);
		pak.read(valBytes);
		int mdlAddr = ValueHandler.getVal(valBytes, false);
		pak.seek(mdlAddr + 108);
		pak.read(valBytes);
		int initBonePos = mdlAddr + ValueHandler.getVal(valBytes, false);
		pak.seek(initBonePos);
		for (int boneCnt = 0; boneCnt < MAX_NUM_BONES; boneCnt++) {
			pak.read(valBytes);
			int boneSize = ValueHandler.getVal(valBytes, false);
			pak.seek(initBonePos + 10);
			pak.read(valBytes);
			int boneIdFromMdl = ValueHandler.getVal(valBytes, false);
			pak.seek(initBonePos);
			byte[] boneBytes = new byte[boneSize];
			pak.read(boneBytes);
			initBonePos += boneSize;
			if (boneIdFromMdl == boneId) {
				for (int posCnt = 0; posCnt < positions.length; posCnt++) {
					System.arraycopy(boneBytes, 48 + posCnt * 4, valBytes, 0, valBytes.length);
					positions[posCnt] = ValueHandler.getValFloat(valBytes, false);
				}
				break;
			}
		}
		return positions;
	}
}