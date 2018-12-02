package rs.raf.os.test;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import rs.raf.os.dir.Directory;
import rs.raf.os.disk.Disk;
import rs.raf.os.disk.SimpleDisk;
import rs.raf.os.fat.FAT16;

public class View {

    private JFrame frame = new JFrame("FAT 16");
    private JPanel panel = new JPanel();
    
    private JPanel fatPanel = new JPanel();
    private JPanel fatValue = new JPanel();
    
    private JPanel diskPanel = new JPanel();
    private LinkedList<Integer> sectors = new LinkedList<>();
    
    private LinkedList<JButton> clusters = new LinkedList<>();
    
    private MockFAT fat;
    private SimpleDisk disk;
    
    public View(MockDirectory dir){
	this.fat = dir.fat;
	this.disk = dir.disk;
	
	frame.setBounds(0, 0, 800, 300);
	frame.setLocationRelativeTo(null);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.add(panel);
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.add(new JLabel("FAT 16: "));
	panel.add(fatPanel);
	//panel.add(new JLabel("FAT Values : "));
	panel.add(fatValue);
	panel.add(Box.createVerticalStrut(20));
	panel.add(new JLabel("DISK: "));
	panel.add(diskPanel);
	
	// redni brojevi klastera
	for(int i = 0; i< fat.getClusterCount() ; i++){
	    JButton b = new JButton(String.valueOf(i));
	    fatPanel.add(b);
	}
	// vrednosti u klasterima
	for(int i = 2; i< fat.getClusterCount() + 2 ; i++){
	    JButton b = new JButton(String.valueOf(fat.readCluster(i)));
	    clusters.add(b);
	    fatValue.add(b);
	}
	
	// oboj neki fajl u zuto
	int nekiFajl = dir.fajlovi.get( dir.fajlovi.keys().nextElement() );
	
	while(nekiFajl!=fat.getEndOfChain()){
	    clusters.get(nekiFajl-2).setBackground(Color.YELLOW);;
	    nekiFajl = fat.readCluster(nekiFajl);
	}
	
	for(int i = 0; i< disk.getSectorCount(); i++){
	    JButton b = new JButton(String.valueOf(i));
		if( (valueOf(disk.readSector(i)) != 0) ) {
			b.setBackground(Color.BLACK);
				}
	    b.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFrame f = new JFrame();
				f.setBounds(0, 0, 200, 200);
				f.setLocationRelativeTo(null);
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JPanel p = new JPanel();
				f.add(p);
				byte[] niz = disk.readSector(Integer.parseInt(b.getText()));
				
				for(int i = 0; i < niz.length; i++) {
					JLabel l = new JLabel(String.valueOf(niz[i]));
					p.add(l);
				}
				f.setVisible(true);
			}
		});
	    sectors.add(i);
	    diskPanel.add(b);
	}
	
	frame.setVisible(true);
    }
    private int valueOf(byte[] b){
	int result = 0;
	for(int i = 0;i < b.length;i++){
	    result+=b[i];
	}
	return result;
    }
    
    public static void main(String[] args) {
    	FAT16 fat = new MockFAT(1, 4);
		
		//sectors are 40 bytes, 10 of them on disk
		Disk disk = new SimpleDisk(40, 10);
		
		MockDirectory dir = new MockDirectory(fat, disk);
		
		//150 bytes of data, should take up four clusters, which are four sectors
		byte[] data = new byte[150];
		for(int i = 0; i < 150; i++) {
			data[i] = (byte)(i*2);
		}
	dir.writeFile("Even", data);
	new View((MockDirectory)dir);
    }

}
