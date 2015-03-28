import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.sound.sampled.*;

/**
 * @author Eric Staykov
 */
public class SineEcho {
	
	private JFrame mainFrame;
	private JLabel frequencyLabel;
	private JLabel delayLabel;
	private static int sampleRate; // samples per second
	private static int delay; // samples to delay playback by to give echo
	private static int delayMS;
	private static int waveFrequency; // Hz
	private static double[] sineValues;
	private static byte[] bufferValues;
	private static byte[] echoValues;
	private static boolean feedback;
	static byte[] buffer;
	AudioFormat af;
	static SourceDataLine sdl;
	
	public SineEcho() throws LineUnavailableException  {
		setupSounds();
		prepareGUI();
	}
	
	public static void main(String[] args) throws LineUnavailableException {
		SineEcho instance = new SineEcho();
		int index = 0, echoIndexStore = 0;
		while (true) {
			echoValues[echoIndexStore] = bufferValues[index];
			int echoIndexPlay = (echoIndexStore - delay < 0) ? (echoIndexStore - delay + sampleRate - 1) : echoIndexStore - delay;
			buffer[0] = (byte) (bufferValues[index] / 2 + echoValues[echoIndexPlay] / 2);
			sdl.write(buffer, 0, 1);
			if (feedback) {
				echoValues[echoIndexStore] = buffer[0];
			}
			index += waveFrequency;
			index %= sampleRate;
			echoIndexStore++;
			echoIndexStore %= sampleRate;
		}
	}
	
	private void prepareGUI() {
		mainFrame = new JFrame("Sine Echo");
		mainFrame.setSize(250, 150);
		mainFrame.setLayout(new GridLayout(5, 1));
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		// frequency text
		frequencyLabel = new JLabel("Frequency = " + waveFrequency + " Hz", JLabel.CENTER);
		mainFrame.add(frequencyLabel);
		// frequency slider
		JSlider frequencySlider = new JSlider(JSlider.HORIZONTAL, 50, 2500, waveFrequency);
		frequencySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				frequencyLabel.setText("Frequency = " + ((JSlider) e.getSource()).getValue() + " Hz");
				waveFrequency = ((JSlider) e.getSource()).getValue();
			}
		});
		mainFrame.add(frequencySlider);
		// delay text
		delayLabel = new JLabel("Delay = " + delayMS + " milliseconds", JLabel.CENTER);
		mainFrame.add(delayLabel);
		// delay slider
		JSlider delaySlider = new JSlider(JSlider.HORIZONTAL, 0, sampleRate - 2, delay);
		delaySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				delay = ((JSlider) e.getSource()).getValue();
				delayMS = (int) (1000.0 * delay / sampleRate);
				delayLabel.setText("Delay = " + delayMS + " milliseconds");
			}
		});
		mainFrame.add(delaySlider);
		// feedback checkbox
		JCheckBox feedbackCheckbox = new JCheckBox("Feedback");
		feedbackCheckbox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				feedback = ((JCheckBox) e.getSource()).isSelected() ? true : false;
			}
		});
		mainFrame.add(feedbackCheckbox);
		mainFrame.setVisible(true);
	}
	
	private void setupSounds() throws LineUnavailableException {
		sampleRate = 44100;
		delay = 10000;
		delayMS = (int) (1000.0 * delay / sampleRate);
		waveFrequency = 440;
		sineValues = new double[sampleRate];
		bufferValues = new byte[sampleRate];
		echoValues = new byte[sampleRate];
		feedback = false;
		for (int index = 0; index < sampleRate; index++) {
			sineValues[index] = Math.sin(2.0 * Math.PI * index / sampleRate);
			bufferValues[index] = (byte) (sineValues[index] * 100);
			echoValues[index] = 0;
		}
		buffer = new byte[1];
		af = new AudioFormat((float) sampleRate, 8, 1, true, false);
		sdl = AudioSystem.getSourceDataLine(af);
		sdl.open();
		sdl.start();
	}
	
}