package edu.cmu.hcii.puc.devices.elevator;

import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.JFrame;

/**
 * Simple Frame containing the main GUI display, an ElevatorPanel object
 */
public class Elevator extends JFrame
{

  private static final String VERSION = "1.2 beta";

  private ElevatorPanel panel;

// Constructors

  public Elevator()
  {
    this(new ElevatorModel());
  }

  public Elevator(ElevatorModel model)
  {
    super("Elevator Simulator " + VERSION);
    panel = new ElevatorPanel(model);

    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    try
    {
      URL url = getClass().getResource("pucproxy.jpg");
      setIconImage(Toolkit.getDefaultToolkit().getImage(url));
    }
    catch (Exception ex)
    {}
    setLocation(new Point(100, 100));
    getContentPane().add(panel);
    pack();
  }

}
