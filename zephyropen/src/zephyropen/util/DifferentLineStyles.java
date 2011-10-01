package zephyropen.util;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.GeneralPath;

public class DifferentLineStyles extends JPanel{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String getName() {
    return "Lines";
    }
    int[] x = new int[] { 50, 100 , 0 }; 
    int[] y = new int[] { 75, 0, 75 }; 

    Stroke[] stroke1 = new Stroke[] {
     new BasicStroke(20.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL),
     new BasicStroke(20.0f, BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER),
     new BasicStroke(20.0f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND), };

     Stroke stroke2 = new BasicStroke(
    		 1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL, 1.0f, new float[]
		 { 6.0f, 2.0f, 1.0f, 2.0f },0.0f);
     
     Font font = new Font("Book Antiqua", Font.BOLD, 15);
     String[] caps = new String[]{ "CAP_BUTT", "CAP_SQUARE", "CAP_ROUND" };
     String[] joins = new String[]{ "JOIN_BEVEL", "JOIN_MITER","JOIN_ROUND" };
     public void paint(Graphics g){
     Graphics2D g2d = (Graphics2D) g;
     GeneralPath path = new GeneralPath();
     path.moveTo(x[0], y[0]); 
     path.lineTo(x[1], y[1]); 
     path.lineTo(x[2], y[2]); 
     g2d.translate(30, 50);

     for (int k = 0; k < stroke1.length; k++) {
     g2d.setColor(Color.red); 
     g2d.setStroke(stroke1[k]);
     g2d.draw(path); 
     g2d.setColor(Color.black); 
     g2d.setStroke(stroke2); 
     g2d.draw(path); 
     g2d.drawString(caps[k], 6, 100); 
     g2d.drawString(joins[k], 6, 110);
     g2d.translate(150, 0);
    }
  }
  public static void main(String[] args){
      JFrame frame = new JFrame("Different Line Styles");
      frame.setContentPane(new DifferentLineStyles());
      frame.setSize(450,210);
      frame.setVisible(true);
  }
}
