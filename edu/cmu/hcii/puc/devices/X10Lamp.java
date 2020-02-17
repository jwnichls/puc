package edu.cmu.hcii.puc.devices;

import java.lang.*; 
import java.util.Vector;

//Queue class

class Queue extends Vector {

    // Puts an item into the queue.
    public void put(Object item) {

        addElement(item);
    }

    // Gets an item from the front of the queue.
    public Object get() {
        Object  obj;
        int     len = size();

        obj = peek();
        removeElementAt(0);

        return obj;
    }

    // Peeks at the front of the queue.
    //exception EmptyQueueException If the queue is empty.

    public Object peek() {
        int     len = size();

        if (len == 0)
                throw new EmptyQueueException();
        return elementAt(0);
    }


    // Returns true if the queue is empty.
    public boolean empty() {
        return size() == 0;
    }

}


// Signals that the queue is empty.

class EmptyQueueException extends RuntimeException {
    /**
     * Constructs a new EmptyQueueException with no detail message.
     * A detail message is a String that describes the exception.
     */
    public EmptyQueueException() {
    }
}


// class X10Lamp

public class X10Lamp implements Runnable {

        private static final String X10DLL = "x10dll";
        private static final int baud_rate = 4800;

	Vector twoway; 

        private boolean stopThread;
        private X10Device m_pX10Device;

	private boolean sendcmd = false;
	private boolean sendrqt = true;

	Thread t;
   	Queue q = new Queue();

	// Static initialization to load the required .dll file

        static {
		try {
		      System.loadLibrary (X10DLL);
	        }
                catch (UnsatisfiedLinkError ule) {
		      System.err.println("ERROR: Failed to load dll from path!");
		      ule.printStackTrace();
		      System.exit(1);
		      // Could attempt to load from within current jar file here
		}
  	}


	// Constructor
        public X10Lamp (X10Device x10dev) { 
		
	    m_pX10Device = x10dev;
	}
	

	// Native Method declarations
	private native void native_open_serial_port (String port_number, int baud_rate) throws DeviceException;

	private native void native_close_serial_port ();
	
	private native void native_Handle_X10_Device (String house_code, String device_number, String command) throws DeviceException;
	
	private native void native_Execute_command (String house_code, String device_number, String command); 

	private native String native_Prime_X10_Interface ();
	
	private native boolean native_read_x10_status ();
	

        public void PrintUsage (String msg) {
                String usage_str = "\nusage: X10Lamp [-com <port number> -x10 <house code> <device code> <command>]\n [-help]\n";
                System.out.println (msg + usage_str);
        }

        public void open_serial_port (String port_number) throws DeviceException {
	       //  System.out.println( "Serial port is opened!" );
                 native_open_serial_port (port_number, baud_rate);
		 stopThread = false;
		 t = new Thread (this);
		 t.start ();
		// System.out.println( "Thread should have been started" );
        }

	public void close_serial_port () {
	        stopThread = true;
		native_close_serial_port ();
		t = null;
	}

	public void Handle_X10_Device (String house_code, String device_number, String command) throws DeviceException {
		native_Handle_X10_Device ( house_code, device_number, command);
	} 

	String Prime_X10_Interface () {
		return (native_Prime_X10_Interface ());
	}
	
	void addqueue (String house_code, String device_number, String command) {

		q.put (house_code);
		q.put (device_number);
		q.put (command);
		sendcmd = true;
	
	}
	
        void vector_x10_object (Vector temp) {
		twoway = (Vector) temp.clone();	
	}
	
	public void run() {

		String house_code, device_number= " ", command, message;
		X10Device.X10Object status_device = null;
		boolean statusRecvFlag = true;
		
		
		int index = 0;

		while (!stopThread) {

			message = Prime_X10_Interface();
							
			if (message.equals ("0x55")) {
				
				if ((q.empty() != true)) {
					house_code    = (String) q.get();
					device_number = (String) q.get();
					command       = (String) q.get();
					try {
					 	Execute_Command (house_code, device_number, command);
												
					} 
					catch (DeviceException dEx) {
						System.err.println("ERROR: " + dEx.getMessage());
					}
				}
				else if ( statusRecvFlag ) {
					if (index >= twoway.size()) 
						index = 0;
					X10Device.X10Object twowayobject = (X10Device.X10Object) twoway.elementAt( index++ );
			
					status_device = twowayobject;
				
					try {
					    System.out.println ("sending status request for " + twowayobject.m_sHouse + " " + twowayobject.m_sDevice );
						Execute_Command (twowayobject.m_sHouse, twowayobject.m_sDevice, "STATUSREQUEST");
						statusRecvFlag = false;

					}
					catch (DeviceException dEx) {
						System.err.println ("ERROR: " + dEx.getMessage());
					}
				}

				try { Thread.sleep( 1500 ); }
				catch( Exception exp ) { }
			}
			else if (message.equals ("0x5a")) {

				//System.out.println (message);
			
				boolean flag = read_x10_status ();

				if ( status_device == null ) continue;
			
				if (sendcmd == true && sendrqt == true) { sendrqt = false; statusRecvFlag = true; continue;}
				
				if (sendcmd == true && sendrqt == false) {

					System.out.println( "Status is " + (flag?"On":"Off") + " for " + status_device.m_sName );
							
					m_pX10Device.dispatchStateEvent( status_device.statename, (flag?"true":"false") );
					
					sendcmd = false; sendrqt = true;
					
				}
	
				if (sendcmd == false && sendrqt == true) {

					System.out.println( "Status is " + (flag?"On":"Off") + " for " + status_device.m_sName );
							
					m_pX10Device.dispatchStateEvent( status_device.statename, (flag?"true":"false") );
										
				}
			
				

				statusRecvFlag = true;
				
			}
			else {
			     try { Thread.sleep( 1000 ); }
			    catch( Exception exp ) { }
			}
		}
	}

	public void Execute_Command (String house_code, String device_number, String command) throws DeviceException {
		native_Execute_command (house_code, device_number, command); 		
	}

	public boolean read_x10_status () {
		return native_read_x10_status ();
	}
}