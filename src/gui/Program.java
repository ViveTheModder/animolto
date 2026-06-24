package gui;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Program {
	private static final String WINDOW_TITLE = "ANIMOLTO";
	
	public static void run() {
		String[] lblTxt = { "Target Costume PAK", "Source Costume PAK", "Source ANM Directory" };
		String[] lblTooltips = {
			"A costume file from the character subject to the ANM scaling.",
			"A costume file from the character whose animations will be resized.",
			"A folder containing the animations that will be resized."
		};
		//Set components
		Box[] boxes = new Box[lblTxt.length];
		Dimension fieldSize = new Dimension(256, 32);
		Dimension minSize = new Dimension(512, 512);
		JButton applyBtn = new JButton("Adjust ANMs");
		JFrame frame = new JFrame(WINDOW_TITLE);
		JLabel[] labels = new JLabel[lblTxt.length];
		JPanel panel = new JPanel();
		JTextField[] fields = new JTextField[lblTxt.length];
		//Set component properties
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		//Add components
		for (int lblCnt = 0; lblCnt < lblTxt.length; lblCnt++) {
			fields[lblCnt] = new JTextField();
			fields[lblCnt].setMinimumSize(fieldSize);
			fields[lblCnt].setMaximumSize(fieldSize);
			fields[lblCnt].setPreferredSize(fieldSize);
			labels[lblCnt] = new JLabel(lblTxt[lblCnt]);
			labels[lblCnt].setAlignmentX(JLabel.LEFT_ALIGNMENT);
			labels[lblCnt].setHorizontalAlignment(JLabel.LEFT);
			labels[lblCnt].setToolTipText(lblTooltips[lblCnt]);
			boxes[lblCnt] = Box.createHorizontalBox();
			boxes[lblCnt].add(labels[lblCnt]);
			boxes[lblCnt].add(Box.createHorizontalStrut(16));
			boxes[lblCnt].add(fields[lblCnt]);
			panel.add(boxes[lblCnt]);
			panel.add(new JLabel(" "));
		}
		panel.add(applyBtn);
		frame.add(panel);
		//Set frame properties
		frame.setMinimumSize(minSize);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}