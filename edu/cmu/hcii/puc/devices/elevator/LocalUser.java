package edu.cmu.hcii.puc.devices.elevator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.*;
import java.net.URL;
import javax.swing.border.*;
import javax.swing.*;

import java.util.Arrays;

/**
 * Implementation of AbstractUser which interacts with the elevator through
 * a local on-screen GUI.
 */
public class LocalUser extends AbstractUser
{

  // String label constants

  private static final String ELEV_POS_STR = "Elevator Location: ";
  private static final String ELEV_DIR_STR = "Elevator Direction: ";
  private static final String ELEV_DOORS_STR = "Doors Are: ";

  private static final String USER_FLOOR_STR = "Floor: ";

  private static final String ENTERING_TEXT = "Entering...";
  private static final String EXITING_TEXT = "Exiting...";

  // Size constants

  private static final Dimension CONTROL_SIZE = new Dimension(210, 300);

  // Static instance count variables

  private static int userNum = 0;

  // Static main method - for testing

  /**
   * This method is convenient for testing the features of the GUI, so I
   * have left it here for that purpose.
   *
   * @param args Ignored
   */
  public static void main(String[] args)
  {
    ElevatorModel model = new ElevatorModel(5);
    Elevator view = new Elevator(model);
    view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    view.show();
    ElevatorConfig config = new ElevatorConfig(model);
    config.show();
  }

  // GUI instance variables

  private JFrame frame;

  private JLayeredPane controlPane;

  private JLabel elevPos;
  private JLabel elevDir;
  private JLabel doors;

  private JPanel outsideControlPane;
  private JLabel userFloor;
  private JToggleButton upButton;
  private JToggleButton downButton;
  private JButton enterButton;

  private JPanel insideControlPane;
  private JToggleButton[] requestButtons;
  private JToggleButton doorOpenButton;
  private JToggleButton doorCloseButton;
  private JButton exitButton;

  private JPanel transitionPane;
  private JLabel transitionLabel;

  private String name;

  // Constructors

  public LocalUser(ElevatorModel model)
  {
    super(model);
    init();
  }

  public LocalUser(ElevatorModel model, int startingLoc)
  {
    super(model, startingLoc);
    init();
  }

  // Initialization methods

  /**
   * This is a very simple extension of JFrame which just catches the disposal
   * of the window and calls the AbstractUser destroy method, to correctly
   * notify the ElevatorModel that this user is gone.
   */
  private class SpecialFrame extends JFrame
  {
    public SpecialFrame(String name)
    {
      super(name);
    }

    public void dispose()
    {
      super.dispose();
      LocalUser.super.destroy();
    }
  }

  /**
   * Initializes this user object and its GUI
   */
  private void init()
  {
    name = "Local User #" + (userNum++) + " (" + getColorName() + ")";
    frame = new SpecialFrame(name);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    try
    {
      URL url = getClass().getResource("pucproxy.jpg");
      frame.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
    }
    catch (Exception ex)
    {}

    JPanel statusPane = createStatusPane();
    controlPane = createControlPane();
    updateStatus();

    haveAccess = new boolean[model.getNumFloors()];
    Arrays.fill(haveAccess, true);

    Border blackline = BorderFactory.createLineBorder(Color.black);
    statusPane.setBorder(blackline);
    controlPane.setBorder(blackline);

    Container contentPane = frame.getContentPane();
    contentPane.add(statusPane, BorderLayout.NORTH);
    contentPane.add(Box.createGlue(), BorderLayout.CENTER);
    contentPane.add(controlPane, BorderLayout.SOUTH);

    frame.pack();
    frame.show();
  }

  private JPanel createStatusPane()
  {
    JPanel statusPane = new JPanel(new GridLayout(4, 1, 10, 10));

    elevPos = new JLabel(ELEV_POS_STR +
                         model.getFloorName(model.getCurrentLoc()),
                         JLabel.CENTER);
    statusPane.add(elevPos);

    elevDir = new JLabel(ELEV_DIR_STR +
                         model.getDirName(model.getCurrentDir()),
                         JLabel.CENTER);
    statusPane.add(elevDir);

    doors = new JLabel(ELEV_DOORS_STR +
                       (model.doorsAreOpen()? "Open" : "Closed"),
                       JLabel.CENTER);
    statusPane.add(doors);

    JLabel colorLabel = new JLabel("Color: " + getColorName(),
                                   JLabel.CENTER);
    statusPane.add(colorLabel);

    return statusPane;
  }

  private JLayeredPane createControlPane()
  {
    JLayeredPane controls = new JLayeredPane();
    controls.setPreferredSize(CONTROL_SIZE);

    outsideControlPane = createOutsideControls();
    insideControlPane = createInsideControls();
    transitionPane = createTransitionPane();

    controls.add(outsideControlPane, JLayeredPane.DEFAULT_LAYER, 0);
    controls.add(insideControlPane, JLayeredPane.DEFAULT_LAYER, 1);
    controls.add(transitionPane, JLayeredPane.DEFAULT_LAYER, 2);

    controls.addComponentListener(new ComponentAdapter() {

      public void componentResized(ComponentEvent e)
      {
        outsideControlPane.setBounds(0, 0, controlPane.getWidth(),
                                     controlPane.getHeight());
        insideControlPane.setBounds(0, 0, controlPane.getWidth(),
                                    controlPane.getHeight());
        transitionPane.setBounds(0, 0, controlPane.getWidth(),
                                    controlPane.getHeight());
      }

    });

    return controls;
  }

  private JPanel createOutsideControls()
  {
    JPanel outsideControls = new JPanel();
    outsideControls.setBounds(0, 0, CONTROL_SIZE.width, CONTROL_SIZE.height);

    JPanel gridPanel = new JPanel(new GridLayout(5, 1, 10, 10));

    JLabel outsideLabel = new JLabel("Outside Elevator");
    outsideLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
    gridPanel.add(outsideLabel);

    userFloor = new JLabel(USER_FLOOR_STR + model.getFloorName(currentLoc));
    gridPanel.add(userFloor);

    upButton = new JToggleButton("UP");
    upButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        requestUp();
        upButton.setEnabled(false);
      }
    });
    gridPanel.add(upButton);

    downButton = new JToggleButton("DOWN");
    downButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        requestDown();
        downButton.setEnabled(false);
      }
    });
    gridPanel.add(downButton);

    updateCallButtons();

    enterButton = new JButton("Enter");
    enterButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        try
        {
          enter();
        }
        catch (ElevatorException eEx)
        {
          JOptionPane.showMessageDialog(frame, eEx.getMessage(),
                                        "Error Entering Elevator",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    enterButton.setEnabled(model.doorsAreOpen());
    gridPanel.add(enterButton);

    outsideControls.add(gridPanel);
    outsideControls.add(Box.createVerticalGlue());
    outsideControls.setOpaque(true);
    return outsideControls;
  }

  private JPanel createInsideControls()
  {
    JPanel insideControls = new JPanel();
    insideControls.setLayout(new BoxLayout(insideControls, BoxLayout.Y_AXIS));
    insideControls.setBounds(0, 0, CONTROL_SIZE.width, CONTROL_SIZE.height);

    JLabel insideLabel = new JLabel("Inside Elevator");
    insideLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
    insideLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    insideControls.add(insideLabel);

    insideControls.add(Box.createVerticalGlue());

    int numFloors = model.getNumFloors();
    Dimension requestButtonSize = new Dimension(30, 30);
    JPanel requestPanel =
        new JPanel(new GridLayout((numFloors + 1) / 2, 2, 20, 20));
    requestButtons = new JToggleButton[numFloors];
    for (int i = 0; i < numFloors; i++)
    {
      requestButtons[i] = new JToggleButton(model.getFloorName(i));
      requestButtons[i].setMaximumSize(requestButtonSize);
      requestButtons[i].addActionListener(new RequestButtonListener(i));
      requestPanel.add(requestButtons[i]);
    }

    insideControls.add(requestPanel);

    insideControls.add(Box.createVerticalGlue());

    JPanel gridPanel = new JPanel(new GridLayout(2, 2, 10, 10));

    doorOpenButton = new JToggleButton("Door Open");
    doorOpenButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        model.setDoorOpenButton();
        doorOpenButton.setEnabled(false);
      }
    });
    gridPanel.add(doorOpenButton);

    doorCloseButton = new JToggleButton("Door Close");
    doorCloseButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        model.setDoorCloseButton();
        doorCloseButton.setEnabled(false);
      }
    });
    gridPanel.add(doorCloseButton);

    exitButton = new JButton("Exit");
    exitButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        try
        {
          exit();
        }
        catch (ElevatorException eEx)
        {
          JOptionPane.showMessageDialog(frame, eEx.getMessage(),
                                        "Error Exiting Elevator",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    exitButton.setEnabled(model.doorsAreOpen());
    gridPanel.add(exitButton);

    insideControls.add(gridPanel);

    insideControls.setOpaque(true);
    return insideControls;
  }

  private JPanel createTransitionPane()
  {
    JPanel transitionPanel = new JPanel();
    transitionPanel.setBounds(0, 0, CONTROL_SIZE.width, CONTROL_SIZE.height);

    transitionLabel = new JLabel();
    transitionPanel.add(transitionLabel);

    transitionPanel.setOpaque(true);
    return transitionPanel;
  }

  // AbstractUser abstract methods

  protected void updateFloor()
  {
    userFloor.setText(USER_FLOOR_STR + model.getFloorName(currentLoc));
  }

  protected void updateStatus()
  {
    updateEntranceButtons();

    if (status == INSIDE)
    {
      insideControlPane.setVisible(true);
      controlPane.moveToFront(insideControlPane);
      outsideControlPane.setVisible(false);
      transitionPane.setVisible(false);
    }
    else if (status == OUTSIDE)
    {
      outsideControlPane.setVisible(true);
      controlPane.moveToFront(outsideControlPane);
      insideControlPane.setVisible(false);
      transitionPane.setVisible(false);
    }
    else
    {
      transitionLabel.setText((status == ENTERING?
                               ENTERING_TEXT : EXITING_TEXT));
      transitionPane.setVisible(true);
      controlPane.moveToFront(transitionPane);
      outsideControlPane.setVisible(false);
      insideControlPane.setVisible(false);
    }
  }

  protected void updateCallButtons()
  {
    boolean upRequested = model.upRequestedAt(currentLoc);
    boolean downRequested = model.downRequestedAt(currentLoc);

    ElevatorModel.debug("Updating call buttons, up = " + upRequested + "; " +
                        "down = " + downRequested);

    upButton.setSelected(upRequested);
    downButton.setSelected(downRequested);

    upButton.setEnabled((!upRequested) &&
                        (currentLoc != (model.getNumFloors() - 1)));
    downButton.setEnabled((!downRequested) &&
                          (currentLoc != 0));

    ElevatorModel.debug("Up: enabled=" + upButton.isEnabled() +
                        "; selected=" + upButton.isSelected());
    ElevatorModel.debug("Down: enabled=" + downButton.isEnabled() +
                        "; selected=" + downButton.isSelected());

    upButton.repaint();
    downButton.repaint();
  }

  protected void updateAccess()
  {
    // Undo any updates to the access array; just give user access to all floors
    Arrays.fill(haveAccess, true);
  }

  protected String getName() { return name; }

  // New utility methods to deal with events

  private void updateEntranceButtons()
  {
    if (status == INSIDE)
    {
      exitButton.setEnabled(model.doorsAreOpen());
    }
    else if (status == OUTSIDE)
    {
      enterButton.setEnabled(model.doorsAreOpen() &&
                             currentLoc == model.getCurrentLoc());
    }
  }

  // ElevatorListener methods

  // Override AbstractUser implementations to catch location events

  public boolean processEvent(ElevatorEvent event)
  {
    if (super.processEvent(event))
      return true;
    else
    {
      if (event instanceof ElevatorEvent.DirChangedEvent)
      {
        ElevatorEvent.DirChangedEvent e =
            (ElevatorEvent.DirChangedEvent) event;
        handleDirChanged(e.dir);
        return true;
      }
      else if (event instanceof ElevatorEvent.DoorOpenEvent)
      {
        ElevatorEvent.DoorOpenEvent e =
            (ElevatorEvent.DoorOpenEvent) event;
        handleDoorOpen(e.started, e.isOpen);
        return true;
      }
      else if (event instanceof ElevatorEvent.DoorCloseButtonEvent)
      {
        ElevatorEvent.DoorCloseButtonEvent e =
            (ElevatorEvent.DoorCloseButtonEvent) event;
        handleDoorCloseButtonSet(e.newVal);
        return true;
      }
      else if (event instanceof ElevatorEvent.DoorOpenButtonEvent)
      {
        ElevatorEvent.DoorOpenButtonEvent e =
            (ElevatorEvent.DoorOpenButtonEvent) event;
        handleDoorOpenButtonSet(e.newVal);
        return true;
      }
      else if (event instanceof ElevatorEvent.FloorRequestButtonEvent)
      {
        ElevatorEvent.FloorRequestButtonEvent e =
            (ElevatorEvent.FloorRequestButtonEvent) event;
        handleFloorRequestButtonSet(e.floor, e.newVal);
        return true;
      }
      else
        return false;
    }
  }

  public void handleFloorReached(int floor)
  {
    super.handleFloorReached(floor);
    elevPos.setText(ELEV_POS_STR + model.getFloorName(floor));
  }

  // Implement other methods as well

  public void handleDirChanged(int dir)
  {
    elevDir.setText(ELEV_DIR_STR + model.getDirName(dir));
  }

  public void handleDoorOpen(boolean started, boolean isOpen)
  {
    if (((started == ElevatorModel.ENDED) && isOpen) ||
        ((started == ElevatorModel.STARTED) && !isOpen))
    {
      updateEntranceButtons();
      doorCloseButton.setEnabled(isOpen);
    }

    String label = ELEV_DOORS_STR;
    if (started == ElevatorModel.STARTED)
      if (isOpen)
        label += "Opening...";
      else
        label += "Closing...";
    else
      if (isOpen)
        label += "Open";
      else
        label += "Closed";

    doors.setText(label);
  }

  public void handleDoorCloseButtonSet(boolean newVal)
  {
    doorCloseButton.setSelected(newVal);
    doorCloseButton.setEnabled(!newVal);
  }

  public void handleDoorOpenButtonSet(boolean newVal)
  {
    doorOpenButton.setSelected(newVal);
    doorOpenButton.setEnabled(!newVal);
  }

  public void handleFloorRequestButtonSet(int floor, boolean newVal)
  {
    requestButtons[floor].setSelected(newVal);
    requestButtons[floor].setEnabled(!newVal && haveAccess[floor]);
  }

  // Inner class listeners

  class RequestButtonListener implements ActionListener
  {
    int fnum;

    private RequestButtonListener(int fnum) { this.fnum = fnum; }

    public void actionPerformed(ActionEvent ae)
    {
      requestFloor(fnum);
      requestButtons[fnum].setEnabled(false);
    }
  }

}