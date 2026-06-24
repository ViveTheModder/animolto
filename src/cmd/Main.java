package cmd;
//ANIMOLTO by ViveTheJoestar
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import gui.Program;

public class Main {

	public static void main(String[] args) throws IOException {
		String helpText = "USAGE: java -jar animolto.jar \"path/to/tgt/mdl/pak\" \"path/to/src/mdl/pak\" \"path/to/folder/with/src/anms\"";
		if (args.length > 2) {
			File boneCsv = new File("./csv/bone-ids.csv");
			String[] boneNames = getBoneNames(boneCsv);
			float[] colVals = { 1 , 1 };
			File[] anmRefs = null, files = new File[3];
			CharaAnm[] anms = null;
			CharaPak[] paks = new CharaPak[2];
			long start = System.currentTimeMillis();
			for (int fileCnt = 0; fileCnt < files.length; fileCnt++) {
				files[fileCnt] = new File(args[fileCnt].replace("?", ""));
				if (!files[fileCnt].isFile()) {
					anmRefs = files[fileCnt].listFiles((dir, name) -> name.toLowerCase().endsWith(".anm"));
					if (anmRefs == null) {
						System.out.println("ERROR: " + files[fileCnt] + " does NOT point to a directory with ANM files!");
						System.exit(1);
					}
					anms = new CharaAnm[anmRefs.length];
					for (int anmCnt = 0; anmCnt < anms.length; anmCnt++) {
						anms[anmCnt] = new CharaAnm(anmRefs[anmCnt]);
						if (!anms[anmCnt].isValid()) {
							System.out.println("ERROR: " + files[fileCnt] + " is NOT a valid ANM file!");
							System.exit(2);
						}
						else if (anmRefs[anmCnt].getName().toLowerCase().contains("enemy"))
							anmRefs[anmCnt] = null;
					}
				}
				else {
					String name = files[fileCnt].getName().toLowerCase();
					boolean matchCostumeName = name.matches("[a-z0-9_]+_\\dp.pak") || name.matches("[a-z0-9_]+_\\dp_dmg.pak");
					if (matchCostumeName) {
						int pakIdx = (fileCnt + 1) / paks.length;
						paks[pakIdx] = new CharaPak(files[fileCnt]);
						if (!paks[pakIdx].isValid()) {
							System.out.println("ERROR: " + files[fileCnt] + " is NOT a valid DBZ BT2 or DBZ BT3 costume PAK file!");
							System.exit(3);
						}
						colVals[pakIdx] = paks[pakIdx].getCollisionX();
					}
				}
			}
			File log = new File(anmRefs[0].getParentFile().getAbsolutePath() + File.separatorChar + "animolto.log");
			FileWriter fw = new FileWriter(log);
			String out = "";
			float coefficient = colVals[0] / colVals[1];
			for (int anmCnt = 0; anmCnt < anms.length; anmCnt++) {
				if (anmRefs[anmCnt] == null) continue;
				String anmName = "[" + anmRefs[anmCnt].getName() + "]";
				System.out.println(anmName);
				out += anmName + "\n";
				int[] boneIds = anms[anmCnt].getTranslationBoneIds();
				for (int boneId: boneIds) {
					if (boneId != 0) {
						CharaAnm newAnm = new CharaAnm(anmRefs[anmCnt]);
						float[] srcBoneCoords = paks[0].getPositions(boneId), dstBoneCoords = paks[1].getPositions(boneId);
						String anmResult = newAnm.writeNewCoordinates(coefficient, srcBoneCoords, dstBoneCoords, boneNames[boneId], boneId);
						System.out.println(anmResult);
						out += anmResult;
					}
				}
			}
			fw.write(out);
			fw.close();
			long end = System.currentTimeMillis();
			System.out.printf("TIME: %.3f seconds.\n", (end - start) / 1000.0);
		}
		else System.out.println(helpText);
	}
	
	private static String[] getBoneNames(File boneCsv) throws IOException {
		String[] boneNames = new String[CharaPak.MAX_NUM_BONES];
		if (boneCsv.exists()) {
			Scanner sc = new Scanner(boneCsv);
			if (sc.hasNextLine()) {
				if (!sc.nextLine().equals("id,name")) {
					sc.close();
					return null;
				}
			}
			int lineCnt = 0;
			while (sc.hasNextLine()) {
				if (lineCnt > boneNames.length) break;
				String line = sc.nextLine();
				String[] lineArray = line.split(",");
				if (lineArray.length == 2) boneNames[lineCnt] = lineArray[1];
				lineCnt++;
			}
			sc.close();
			return boneNames;
		}
		return null;
	}
}