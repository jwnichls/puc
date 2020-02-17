package edu.cmu.hcii.puc.devices;

import com.maya.puc.proxy.PUCProxy;

import java.awt.Color;

import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.*;

/**
 * Implementation of a dialog used to configure the IP Address of a running
 * PUC device.  A standard use of this class might look something like the
 * following:
 *
 * public void configure()
 * {
 *   String newIP = new IPDialog().getNewIP();
 *   if (newIP != null)
 *   {
 *     // Store whether or not the device was running when the configure
 *     // was entered
 *
 *     // Change IP address accordingly
 *
 *     // If device was running, and needs to be restarted, restart it
 *   }
 * }
 */
public class IPDialog extends JDialog
{

  private InputVerifier ipVerifier = new IPInputVerifier();

  private IPTextField[] addr;
  private boolean accepted;

  private JPanel addrPanel;

  /**
   * Constructor which does all the necessary GUI setup
   */
  public IPDialog()
  {
    super(PUCProxy.getInstance().getFrame(), "Configure IP Address", true);

    getContentPane().setLayout(new GridLayout(2, 1));
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    setBackground(Color.black);

    addrPanel = new JPanel();

    addr = new IPTextField[4];
    for (int i = 0; i < 3; i++)
    {
      addr[i] = new IPTextField();
      addrPanel.add(addr[i]);
      addrPanel.add(new JLabel("."));
    }
    addr[3] = new IPTextField();
    addrPanel.add(addr[3]);

    getContentPane().add(addrPanel);

    JPanel buttonPanel = new JPanel();

    JButton okButton = new JButton("OK");
    okButton.addActionListener(
        new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        accepted = true;
        dispose();
      }
    });
    buttonPanel.add(okButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(
        new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        dispose();
      }
    });
    buttonPanel.add(cancelButton);

    getContentPane().add(buttonPanel);

    pack();
  }

  /**
   * Initializes the text fields
   */
  private void init()
  {
    accepted = false;

    for (int i = 0; i < 3; i++)
    {
      addr[i].setText("");
    }
  }

  public void show()
  {
    init();
    super.show();
  }

  /**
   * The main method to use with this class.  Once an IPDialog has been
   * constructed, call its getNewIP() method to show it, receive the input,
   * and return the new address as a String.
   *
   * @return If the IP address entered is valid, and the dialog was closed as
   * a return of the user pressing the "OK" button, then the entered IP
   * address is returned.  Otherwise, null is returned.
   */
  public String getNewIP()
  {
    show();

    if (accepted && (ipVerifier.verify(addr[0]) &&
                     ipVerifier.verify(addr[1]) &&
                     ipVerifier.verify(addr[2]) &&
                     ipVerifier.verify(addr[3])))
    {
      String newAddr =
          addr[0].getText() + "." +
          addr[1].getText() + "." +
          addr[2].getText() + "." +
          addr[3].getText();
      return newAddr;
    }
    else
      return null;
  }

  /**
   * Text field class designed to be used in a group of 4 to get an IP address.
   * Each field has a width of 3 characters, a horizontal alignment of center,
   * and an input verifier which ensures that the text field cannot lose focus
   * unless its content is an integer between 0 and 255 inclusive.
   *
   * Also, if a period (".") is typed, the IPTextField assumes the user wanted
   * to tab to the next portion of the IP Address, so it turns the period
   * key event into a tab key event.
   */
  private class IPTextField extends JTextField
  {

    private IPTextField()
    {
      super(3);
      setHorizontalAlignment(JTextField.CENTER);
      setInputVerifier(ipVerifier);

      super.addKeyListener(
          new KeyListener()
      {
        public void keyPressed(KeyEvent e)
        {
          int key = e.getKeyCode();
          if (key == e.VK_PERIOD)
          {
            e.setKeyCode(e.VK_TAB);
          }
        }

        public void keyReleased(KeyEvent e)
        {
          int key = e.getKeyCode();
          if (key == e.VK_PERIOD)
          {
            e.setKeyCode(e.VK_TAB);
          }
        }

        public void keyTyped(KeyEvent e)
        {
          char key = e.getKeyChar();
          if (key == '.')
          {
            e.consume();
          }
        }
      });
    }

  }

  /**
   * Verifies that the content of the IPTextField is an integer between 0 and
   * 255, inclusive.
   */
  private class IPInputVerifier extends InputVerifier
  {

    public boolean verify(JComponent comp)
    {
      if (comp instanceof IPTextField)
      {
        IPTextField iptf = (IPTextField) comp;
        try
        {
          int val = Integer.parseInt(iptf.getText());
          if (val >= 0 && val < 256)
            return true;
        }
        catch (NumberFormatException nfEx)
        {
          return false;
        }
      }

      return false;
    }
  }


}