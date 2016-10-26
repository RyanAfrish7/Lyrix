package aryanware.lyrix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import com.sun.jna.*;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

public class Lyrix {

	final JFrame jFrame;
	final Vector<LyrixLine> ly;

	EmbeddedMediaPlayerComponent component;

	/**
	 *
	 * @param 	f	                    Path to the lyric file
	 * @return		                    Vector consisting of all the LyrixLines
	 * @throws	FileNotFoundException
	 */
	protected Vector<LyrixLine> parseFile(String f) throws FileNotFoundException {
		Scanner sc = new Scanner(new FileReader(new File(f)));

		Vector<LyrixLine> v = new Vector<>();

		v.add(new LyrixLine("(BEGIN)"));

		while(sc.hasNextLine()) {
			String s = sc.nextLine();
			if(!(s.trim().isEmpty() || s.charAt(0) == '\"' || s.charAt(0) == '['))
				v.add(new LyrixLine(s));
		}

		v.add(new LyrixLine("(END)"));

		sc.close();

		return v;
	}

	/**
	 *
	 * @param	v	Vector of LyrixLines
	 * @param	f	Filename of the .srt file to be created
	 * @throws	IOException
	 */
	protected void createSRT(Vector<LyrixLine> v, String f) throws IOException {
		PrintWriter p = new PrintWriter(new FileWriter(f));

		for(int lineno = 0; lineno < ly.size(); lineno++) {
			p.println(lineno);
			p.print(v.elementAt(lineno) + "\n\n");
		}

		p.close();
	}

	protected EmbeddedMediaPlayerComponent readyMedia() {
		final EmbeddedMediaPlayerComponent c = new EmbeddedMediaPlayerComponent();

		c.getMediaPlayer().playMedia("~/Videos/One Direction - Perfect.mp4.mp4");
		c.getMediaPlayer().setEnableMouseInputHandling(false);
		c.getMediaPlayer().setEnableKeyInputHandling(false);
		c.getVideoSurface().requestFocusInWindow();
		c.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void playing(MediaPlayer mediaPlayer) {
				c.getMediaPlayer().setSpu(-1);
				c.getMediaPlayer().pause();
			}
		});

		return c;
	}

	public Lyrix() throws FileNotFoundException {

		jFrame = new JFrame("Lyrix by ARyanware");
		final JPanel jPanel = new JPanel();
		final JLabel jLabel = new JLabel("Drag a video file into the window", JLabel.CENTER);

		jLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
		jLabel.setBorder(new EmptyBorder(14, 0, 14, 0));
		jLabel.setOpaque(true);
		jLabel.setBackground(Color.BLACK);
		jLabel.setForeground(Color.WHITE);

		jFrame.setSize(920, 540);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		jFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				component.release();
			}
		});

		jFrame.setTransferHandler(new TransferHandler() {
			@Override
			public boolean canImport(TransferSupport support) {
				if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	                return false;
	            }

	            boolean copySupported = (COPY & support.getSourceDropActions()) == COPY;

	            if (!copySupported) {
	            	return false;
	            }

	            support.setDropAction(COPY);

	            return true;
			}

			@Override
			public boolean importData(TransferSupport support) {
				if (!canImport(support)) {
	                return false;
	            }

	            Transferable t = support.getTransferable();

	            try {
	                java.util.List<File> l =
	                    (java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);

	                // Checking for many or no files
	                if(l.size() != 1)
	                	return false;

	                component = new EmbeddedMediaPlayerComponent();
	                jPanel.add(component, BorderLayout.CENTER);
	                component.getMediaPlayer().playMedia(l.get(0).getAbsolutePath());

	            } catch (UnsupportedFlavorException e) {
	                return false;
	            } catch (IOException e) {
	                return false;
	            }
	            return true;
			}
		});

		ly = parseFile("lyrics.ly");

		KeyAdapter k = new KeyAdapter() {

			long LastTime = 0;
			int lineno = 0;

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO improve key listener catching
				switch(e.getKeyCode()) {
					case KeyEvent.VK_SPACE:
						if(lineno < ly.size() - 1) {
							ly.elementAt(lineno).setTime(LastTime, LastTime = component.getMediaPlayer().getTime());
							lineno++;
						}
						break;
					case KeyEvent.VK_BACK_SPACE:
						if(lineno > 0) {
							lineno--;
							LastTime = ly.elementAt(lineno).startTime;
						} else LastTime = 0;
						component.getMediaPlayer().setTime(LastTime);
						break;
					case KeyEvent.VK_ENTER:
						try {
							createSRT(ly, "something.srt");
						} catch (IOException e1) {
							System.err.println(e1);
							e1.printStackTrace();
						}
						break;
					default:
						return;
				}
				//System.err.println(e.getKeyCode());
				jLabel.setText(ly.elementAt(lineno).text);
			}
		};

		jFrame.addKeyListener(k);

		jPanel.setLayout(new BorderLayout());
		jPanel.add(jLabel, BorderLayout.SOUTH);


		jFrame.setContentPane(jPanel);
		jFrame.setVisible(true);
	}

	public static void main(final String[] args) {
		NativeLibrary.addSearchPath("libvlc", "C:\\Program Files (x86)\\VideoLAN\\VLC");
		NativeLibrary.addSearchPath("libvlc", "/usr/lib/");
		try {
			if(!new NativeDiscovery().discover())
				throw new UnsupportedOperationException("Unable to detect libVLC");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						new Lyrix();
					}
					catch(FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			});
		}
		catch(UnsupportedOperationException e) {
			System.err.println("Unable to detect VLC media player");
		}
	}
}
