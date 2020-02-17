package edu.cmu.hcii.puc.devices.UPnP.AxisCamera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

/**
 * Utility class for parsing the proprietary Axis format for sending the list
 * of presets.
 */

public class PresetList extends ArrayList
{

  public PresetList()
  {
  }

  public Preset getPreset(int i)
  {
    return (Preset)super.get(i);
  }

  /**
   * Takes the proprietary format for the list returned by the Axis camera,
   * parses it, and regenerates this ArrayList using the entries parsed.
   *
   * @param presetList The proprietary String returned from the Axis camera
   */
  public void parseUPnPPresetList(String presetList)
  {
    clear();
    System.out.println("preset list string is: " + presetList);
    StringTokenizer st = new StringTokenizer(presetList, ",=");
    while (st.hasMoreTokens())
    {
      String posNum = st.nextToken();
      try
      {
        int presetNum = Integer.parseInt(posNum.substring(11));
        String presetName = st.nextToken();
        System.out.println("Adding preset: " + presetName + " (#" + presetNum + ")");
        addPreset(presetNum, presetName);
      }
      catch (NumberFormatException nfEx)
      {
        st.nextToken();
      }
    }
  }

  public void addPreset(int presetNum, String presetName)
  {
    Preset p = new Preset(presetNum, presetName);
    super.add(p);
    Collections.sort(this);
  }

  /**
   * A Preset object, encapsulating its name and number
   */
  public static class Preset implements Comparable
  {

    private int presetNum;
    private String presetName;

    public Preset(int presetNum, String presetName)
    {
      this.presetNum = presetNum;
      this.presetName = presetName;
    }

    public int getPresetNum() { return presetNum; }
    public String getPresetName() { return presetName; }

    public int compareTo(Object o)
    {
      if (o instanceof Preset)
      {
        Preset p = (Preset) o;
        return presetNum - p.getPresetNum();
      }
      else throw new ClassCastException();
    }

  }

}