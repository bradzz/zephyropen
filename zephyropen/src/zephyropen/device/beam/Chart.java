package zephyropen.device.beam;
import javax.swing.*;
import java.awt.*;

public class Chart {

	
	public Color blue = new Color(0, 173, 239);
	public Color red = new Color(0, 173, 239);
	public Color yellow = new Color(0, 173, 239);

		public static void main(String[] args) {
			Chart d = new Chart();
		}

		public Chart(){
			JFrame frame = new JFrame("Drawing colorfull shapes");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(new MyComponent());
			frame.setSize(600,400);
			frame.setVisible(true);	
		}

		public class MyComponent extends JComponent{
			public void paint(Graphics g){
				  
				
			
				int height = 320;
				int width = 300;
				g.setColor(Color.red);
				g.fillOval(250,20,height,width);
				g.setColor(Color.blue);
				g.fillOval(270,30,height/2,width/2); 
				g.setColor(new Color(0, 173, 239)); 
				g.fillOval(300,30,height/3,width/3); 
					
				// grid lines 
				int w = getWidth();
		        int h = getHeight();
		        g.setColor(Color.black); 
		        // g2.setStroke(new BasicStroke(3));
		        g.drawLine(0, h/2, w, h/2);  
		        g.drawLine(w/2, 0,w/2, h);
				
		}
	}
}