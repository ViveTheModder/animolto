package gui;
//ANIMOLTO GUI by ViveTheJoestar
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.Runnable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import cmd.CharaAnm;
import cmd.CharaPak;
import cmd.Main;

public class Program {
	private static File currDir;
	private static final String HTML_START = "<html><div style='font-size: 14px; font-style: italic;'>";
	private static final String HTML_END = "</div></html>";
	private static final String WINDOW_TITLE = "ANIMOLTO v2.0";
	
	public static void launch(String[] boneNames) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			run(boneNames, tk);
		}
		catch (Exception e) {
			errorDialog(e, tk);
		}
	}
	
	private static File getFilefromChooser(String lblText, Toolkit tk, boolean isFolder) {
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Character Costume File (.pak)", "pak");
		if (!isFolder) {
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(filter);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		else fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Open " + lblText + "...");
		if (currDir != null) fc.setCurrentDirectory(currDir);
		int result = -1;
		while (result == -1) {
			result = fc.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File dir = fc.getSelectedFile();
				currDir = dir;
				if (!isFolder) {
					if (dir.isFile() && dir.getName().toLowerCase().endsWith(".pak")) return dir;
					else {
						result = -1;
						errorDialog("Chosen directory is NOT a valid DBZ BT2 or DBZ BT3 costume PAK file!", tk);
					}
				}
				return dir;
			}
		}
		return null;
	}
	private static JTextField getTextField(Dimension fieldSize, String lblText) {
		Font tfFont = new Font("Tahoma", Font.ITALIC, 12);
		JTextField tf = new JTextField();
		String placeholder = "Enter path to " + lblText + ", or click the [+] button...";
		tf.setForeground(Color.GRAY);
		tf.setFont(tfFont);
		tf.setMinimumSize(fieldSize);
		tf.setMaximumSize(fieldSize);
		tf.setPreferredSize(fieldSize);
		tf.setText(placeholder);
		tf.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (tf.getText().equals(placeholder)) {
					tf.setForeground(Color.BLACK);
					tf.setText("");
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if (tf.getText().isEmpty()) {
					tf.setForeground(Color.GRAY);
					tf.setText(placeholder);
				}
			}	
		});
		return tf;
	}
	private static void errorBeep(Toolkit tk) {
		if (System.getProperty("os.name").contains("Win")) {
			Runnable sndThread = (Runnable) tk.getDesktopProperty("win.sound.exclamation");
			if (sndThread != null) sndThread.run();
		}
	}
	private static void errorDialog(Exception e, Toolkit tk) {
		String errMsg = e.getClass().getSimpleName() + ": " + e.getMessage() + "\n";
		StackTraceElement[] stackTraceArr = e.getStackTrace();
		for (StackTraceElement ste: stackTraceArr) {
			String line = ste.toString().replace("[", "").replace("]", "");
			if (line.startsWith("gui") || line.startsWith("cmd")) errMsg += line + "\n";
		}
		errorBeep(tk);
		JOptionPane.showMessageDialog(null, errMsg, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
	}
	private static void errorDialog(String errMsg, Toolkit tk) {
		errorBeep(tk);
		JOptionPane.showMessageDialog(null, errMsg, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
	}
	private static void run(String[] boneNames, Toolkit tk) {
		String[] lblTxt = { "Target Costume PAK", "Source Costume PAK", "Source ANM Directory" };
		String[] lblTooltips = {
			"A costume file from the character subject to the ANM scaling.",
			"A costume file from the character whose animations will be resized.",
			"A folder containing the animations that will be resized."
		};
		final File[] dirs = new File[lblTxt.length];
		//Set components
		Box btnBox = Box.createHorizontalBox();
		Box[] boxes = new Box[lblTxt.length];
		Color bgColor = new Color(34, 31, 30);
		Color btnColor = new Color(202, 87, 87);
		Color[] lblColors = { new Color(109, 103, 202), new Color(117, 202, 135), new Color(146, 152, 53) };
		Dimension btnSize = new Dimension(64, 32);
		Dimension fieldSize = new Dimension(384, 32);
		Dimension labelSize = new Dimension(160, 32);
		Dimension minSize = new Dimension(768, 488);
		Font btnFont = new Font("Tahoma", Font.BOLD, 28);
		Font lblFont = new Font("Tahoma", Font.BOLD, 14);
		JButton applyBtn = new JButton("Adjust ANMs");
		JButton[] openBtns = new JButton[lblTxt.length];
		JFrame frame = new JFrame(WINDOW_TITLE);
		JLabel[] labels = new JLabel[lblTxt.length];
		JPanel panel = new JPanel();
		JTextField[] fields = new JTextField[lblTxt.length];
		//Set component properties
		applyBtn.setBackground(btnColor);
		applyBtn.setContentAreaFilled(false);
		applyBtn.setForeground(Color.WHITE);
		applyBtn.setFont(btnFont);
		applyBtn.setOpaque(true);
		panel.setBackground(bgColor);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		//Add & set components
		panel.add(Box.createVerticalGlue());
		for (int lblCnt = 0; lblCnt < lblTxt.length; lblCnt++) {
			final int index = lblCnt;
			fields[lblCnt] = getTextField(fieldSize, lblTxt[lblCnt]);
			labels[lblCnt] = new JLabel(lblTxt[lblCnt]);
			labels[lblCnt].setAlignmentX(JLabel.RIGHT_ALIGNMENT);
			labels[lblCnt].setForeground(lblColors[lblCnt]);
			labels[lblCnt].setFont(lblFont);
			labels[lblCnt].setHorizontalAlignment(JLabel.RIGHT);
			labels[lblCnt].setMinimumSize(labelSize);
			labels[lblCnt].setMaximumSize(labelSize);
			labels[lblCnt].setPreferredSize(labelSize);
			labels[lblCnt].setToolTipText(HTML_START + lblTooltips[lblCnt] + HTML_END);
			openBtns[lblCnt] = new JButton(" + ");
			openBtns[lblCnt].setBackground(lblColors[lblCnt]);
			openBtns[lblCnt].setContentAreaFilled(false);
			openBtns[lblCnt].setForeground(Color.WHITE);
			openBtns[lblCnt].setFont(lblFont);
			openBtns[lblCnt].setMinimumSize(btnSize);
			openBtns[lblCnt].setMaximumSize(btnSize);
			openBtns[lblCnt].setPreferredSize(btnSize);
			openBtns[lblCnt].setOpaque(true);
			openBtns[lblCnt].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					boolean isFolder = lblTxt[index].contains("Directory");
					dirs[index] = getFilefromChooser(lblTxt[index], tk, isFolder);
					if (dirs[index] != null) {
						fields[index].setBackground(Color.LIGHT_GRAY);
						fields[index].setEditable(false);
						fields[index].setForeground(Color.GRAY);
						fields[index].setText(dirs[index].getAbsolutePath());
					}
					else {
						fields[index].setBackground(Color.WHITE);
						fields[index].setEditable(true);
						fields[index].setForeground(Color.BLACK);
						fields[index].setText("");
					}
				}
			});
			boxes[lblCnt] = Box.createHorizontalBox();
			boxes[lblCnt].add(labels[lblCnt]);
			boxes[lblCnt].add(Box.createHorizontalStrut(16));
			boxes[lblCnt].add(fields[lblCnt]);
			boxes[lblCnt].add(Box.createHorizontalStrut(16));
			boxes[lblCnt].add(openBtns[lblCnt]);
			panel.add(boxes[lblCnt]);
			panel.add(new JLabel(" "));
		}
		applyBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					String errMsg = "";
					for (int lblCnt = 0; lblCnt < lblTxt.length; lblCnt++) {
						boolean isFolder = lblTxt[lblCnt].contains("Directory");
						if (dirs[lblCnt] != null) {
							//Check for manually entered paths and turn them to directories/files
							if (fields[lblCnt].getBackground().equals(Color.LIGHT_GRAY))
								dirs[lblCnt] = new File(fields[lblCnt].getText());
							//Validate paths regardless of how they were entered
							if (isFolder) {
								if (!dirs[lblCnt].isDirectory()) {
									errMsg += lblTxt[lblCnt] + " does NOT point to a directory!\n";
									dirs[lblCnt] = null;
								}
							}
							else if (!dirs[lblCnt].isFile()) {
								errMsg += lblTxt[lblCnt] + " does NOT point to a file!\n";
								dirs[lblCnt] = null;
							}
							//Validate file contents
							if (!isFolder) {
								CharaPak tmpPak = new CharaPak(dirs[lblCnt]);
								if (!tmpPak.isValid()) {
									errMsg += lblTxt[lblCnt] + " has invalid PAK contents!\n";
									dirs[lblCnt] = null;
								}
							}
						}
						else errMsg += lblTxt[lblCnt] + " has NOT been set to anything!\n";
					}
					//Proceed with actual operations once all directories are valid
					if (!errMsg.equals("")) errorDialog(errMsg, tk);
					else {
						long start = System.currentTimeMillis();
						CharaPak[] paks = { new CharaPak(dirs[0]), new CharaPak(dirs[1]) }; 
						boolean bigEndian = paks[1].isInBigEndian();
						File[] anmFiles = dirs[2].listFiles(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								if (name.toLowerCase().endsWith(".anm")) {
									try {
										File anmFile = new File(dir.getAbsolutePath() + File.separator + name);
										CharaAnm anm = new CharaAnm(anmFile, bigEndian);
										return anm.isValid();
									} 
									catch (IOException e) {
										errorDialog(e, tk);
									}	
								}
								return false;
							}
						});
						if (anmFiles == null) {
							String msg = "Directory is invalid, so no ANM files were found!";
							errorDialog(msg, tk);
							return;
						}
						if (anmFiles.length == 0) {
							String msg = "No ANM files were found, so there is nothing to adjust.";
							tk.beep();
							JOptionPane.showMessageDialog(null, msg, WINDOW_TITLE, JOptionPane.WARNING_MESSAGE);
							return;
						}
						CharaAnm[] anms = new CharaAnm[anmFiles.length];
						String logDateTime = new SimpleDateFormat(Main.DT_FORMAT).format(new Date());
						new File("./log/").mkdir();
						File log = new File("./log/animolto-" + logDateTime + ".log");
						FileWriter fw = new FileWriter(log);
						String out = "";
						float coefficient = paks[0].getCollisionX() / paks[1].getCollisionX();
						for (int anmCnt = 0; anmCnt < anms.length; anmCnt++) {
							String anmDateTime = new SimpleDateFormat(Main.DT_FORMAT).format(new Date());
							String anmName = "[" + anmDateTime + ": " + anmFiles[anmCnt].getName() + "]";
							out += anmName + "\n";
							anms[anmCnt] = new CharaAnm(anmFiles[anmCnt], bigEndian);
							int[] boneIds = anms[anmCnt].getTranslationBoneIds();
							for (int boneId: boneIds) {
								if (boneId != 0) {
									CharaAnm newAnm = new CharaAnm(anmFiles[anmCnt], bigEndian);
									float[] srcBoneXyz = paks[0].getPositions(boneId), dstBoneXyz = paks[1].getPositions(boneId);
									String anmResult = newAnm.writeNewCoordinates(coefficient, srcBoneXyz, dstBoneXyz, boneNames[boneId], boneId);
									out += anmResult;
								}
							}
						}
						fw.write(out);
						fw.close();
						long end = System.currentTimeMillis();
						tk.beep();
						String timeStr = String.format("%.3f", (end - start) / 1000.0);
						String msg = anms.length + " ANM files adjusted in " + timeStr + " seconds! Check " + log + " for detailed information.";
						JOptionPane.showMessageDialog(null, msg, WINDOW_TITLE, JOptionPane.INFORMATION_MESSAGE);
					}
				}
				catch (Exception e) {
					errorDialog(e, tk);
				}
			}
		});
		btnBox.add(Box.createHorizontalGlue());
		btnBox.add(applyBtn);
		btnBox.add(Box.createHorizontalGlue());
		panel.add(btnBox);
		panel.add(Box.createVerticalGlue());
		frame.add(panel);
		//Set frame properties
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(minSize);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}