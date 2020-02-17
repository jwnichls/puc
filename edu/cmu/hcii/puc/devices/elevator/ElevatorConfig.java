package edu.cmu.hcii.puc.devices.elevator;

import javax.swing.*;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
// import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple configuring frame which contains 2 buttons, one to spawn a new
 * local user and another to teleport a user.
 */
public class ElevatorConfig extends JFrame
{

  // Instance variables

  private ElevatorModel model;
  private ArrayList users;

  // Constructors

  public ElevatorConfig(ElevatorModel model) // throws HeadlessException
  {
    super("Elevator Configurer");
    this.model = model;
    init();
  }

  // Public methods

  // Private methods

  private void init()
  {
    users = model.getUsers();
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

    try
    {
      URL url = getClass().getResource("pucproxy.jpg");
      setIconImage(Toolkit.getDefaultToolkit().getImage(url));
    }
    catch (Exception ex)
    {}


    JPanel buttonPane = new JPanel();

    JButton newLocalUser = new JButton("Spawn new local user");
    newLocalUser.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        spawnNewUser();
      }
    });
    buttonPane.add(newLocalUser);

    JButton teleport = new JButton("Teleport :)");
    teleport.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        Object[] userArray = users.toArray();
        AbstractUser user = (AbstractUser) JOptionPane.showInputDialog
            (ElevatorConfig.this, "Select user", "Teleport",
             JOptionPane.PLAIN_MESSAGE, null, userArray, userArray[0]);
        if (user != null)
        {
          String[] floors = model.getFloorNames();
          String floorName = (String) JOptionPane.showInputDialog
              (ElevatorConfig.this, "Select floor", "Teleport",
               JOptionPane.PLAIN_MESSAGE, null, floors, floors[0]);
          if (floorName != null)
          {
            int floor = (Integer.parseInt(floorName) - 1);
            boolean success = user.teleport(floor);
            if (! success)
            {
              JOptionPane.showMessageDialog
                  (ElevatorConfig.this,
                   "Teleport failed: user was in transition",
                   "Teleport Failed!", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }
    });
    buttonPane.add(teleport);

    getContentPane().add(buttonPane);
    pack();
  }

  private void spawnNewUser()
  {
    new LocalUser(model);
  }

}
