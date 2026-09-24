package cmd;
//ANIMOLTO by ViveTheJoestar
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import gui.Program;

public class Main {
	public static final String DT_FORMAT = "yyyy-MM-dd-hh-mm-ss";
	public static void main(String[] args) throws IOException {
		File boneCsv = new File("./csv/bone-ids.csv");
		String[] boneNames = getBoneNames(boneCsv);
		if (boneNames == null) {
			System.out.println("ERROR: " + boneCsv + " does NOT point to the required bone names CSV!");
			System.exit(1);
		}
		String helpText = "USAGE: java -jar animolto.jar \"path/to/tgt/mdl/pak\" \"path/to/src/mdl/pak\" \"path/to/folder/with/src/anms/or/cmas\"";
		if (args.length > 0) {
			if (args[0].equals("-h")) {
				System.out.println(helpText);
				System.exit(0);
			}
		}
		if (args.length > 2) {
			float[] colVals = { 1 , 1 };
			File[] srcRefs = null, files = new File[3];
			CharaAnm[] anms = null;
			CharaCma[] cmas = null;
			CharaPak[] paks = new CharaPak[2];
			long start = System.currentTimeMillis();
			for (int fileCnt = 0; fileCnt < files.length; fileCnt++) {
				files[fileCnt] = new File(args[fileCnt].replace("?", ""));
				if (files[fileCnt].isFile()) {
					String name = files[fileCnt].getName().toLowerCase();
					boolean matchCostumeName = name.matches("[a-z0-9_]+_\\dp.pak") || name.matches("[a-z0-9_]+_\\dp_dmg.pak");
					if (matchCostumeName) {
						int pakIdx = (fileCnt + 1) / paks.length;
						paks[pakIdx] = new CharaPak(files[fileCnt]);
						if (!paks[pakIdx].isValid()) {
							System.out.println("ERROR: " + files[fileCnt] + " is NOT a valid DBZ BT2 or DBZ BT3 costume PAK file!");
							System.exit(2);
						}
						colVals[pakIdx] = paks[pakIdx].getCollisionX();
					}
				}
				else {
					boolean bigEndian = false;
					if (fileCnt == 2) bigEndian = paks[1].isInBigEndian();
					srcRefs = files[fileCnt].listFiles((dir, name) -> name.toLowerCase().endsWith(".anm"));
					if (srcRefs.length != 0) {
						anms = new CharaAnm[srcRefs.length];
						for (int anmCnt = 0; anmCnt < anms.length; anmCnt++) {
							anms[anmCnt] = new CharaAnm(srcRefs[anmCnt], bigEndian);
							if (!anms[anmCnt].isValid()) srcRefs[anmCnt] = null;
							else if (srcRefs[anmCnt].getName().toLowerCase().contains("enemy"))
								srcRefs[anmCnt] = null;
						}
					}
					else {
						srcRefs = files[fileCnt].listFiles((dir, name) -> name.toLowerCase().endsWith(".cma"));
						if (srcRefs.length != 0) {
							cmas = new CharaCma[srcRefs.length];
							for (int cmaCnt = 0; cmaCnt < cmas.length; cmaCnt++) {
								cmas[cmaCnt] = new CharaCma(srcRefs[cmaCnt], bigEndian);
								if (!cmas[cmaCnt].isValid()) srcRefs[cmaCnt] = null;
							}
						}
						else {
							System.out.println("ERROR: " + files[fileCnt] + " does NOT point to a directory with ANM or CMA files!");
							System.exit(3);
						}
					}
				}
			}
			String logDateTime = new SimpleDateFormat(DT_FORMAT).format(new Date());
			new File("./log/").mkdir();
			File log = new File("./log/animolto-" + logDateTime + ".log");
			FileWriter fw = new FileWriter(log);
			String out = "";
			boolean bigEndian = paks[1].isInBigEndian();
			float coefficient = colVals[0] / colVals[1];
			if (anms != null) {
				for (int anmCnt = 0; anmCnt < anms.length; anmCnt++) {
					if (srcRefs[anmCnt] == null) continue;
					String anmName = "[" + srcRefs[anmCnt].getName() + "]";
					System.out.println(anmName);
					anmName = anmName.replace("[", "[" + new SimpleDateFormat(DT_FORMAT).format(new Date()) + ": ");
					out += anmName + "\n";
					int[] boneIds = anms[anmCnt].getTranslationBoneIds();
					for (int boneId: boneIds) {
						if (boneId != 0) {
							CharaAnm newAnm = new CharaAnm(srcRefs[anmCnt], bigEndian);
							float[] srcBoneCoords = paks[0].getPositions(boneId), dstBoneCoords = paks[1].getPositions(boneId);
							String anmResult = newAnm.writeNewCoordinates(coefficient, srcBoneCoords, dstBoneCoords, boneNames[boneId], boneId);
							System.out.println(anmResult);
							out += anmResult;
						}
					}
				}
			}
			else if (cmas != null) {
				for (int cmaCnt = 0; cmaCnt < cmas.length; cmaCnt++) {
					if (srcRefs[cmaCnt] == null) continue;
					String cmaName = "[" + srcRefs[cmaCnt].getName() + "]";
					System.out.println(cmaName);
					cmaName = cmaName.replace("[", "[" + new SimpleDateFormat(DT_FORMAT).format(new Date()) + ": ");
					out += cmaName + "\n";
					String cmaResult = cmas[cmaCnt].writeNewOrdinates(coefficient);
					System.out.println(cmaResult);
					out += cmaResult;
				}
			}
			fw.write(out);
			fw.close();
			long end = System.currentTimeMillis();
			System.out.printf("TIME: %.3f seconds.\n", (end - start) / 1000.0);
		}
		else Program.launch(boneNames);
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