package ca.efriesen.lydia_common;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import java.io.*;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService {
	// Debugging
	private static final boolean D = true;
	private static final String TAG = "Lydia hardware Bluetooth Service";

	// Name for the SDP record when creating server socket
	private static final String NAME = "Lydia";

	// Unique UUID for this application
	public static final UUID MY_UUID = UUID.fromString("f4657ea0-20c3-11e2-81c1-0800200c9a66");

	// Member fields
	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;       // we're doing nothing
	public static final int STATE_LISTEN = 1;     // now listening for incoming connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
	public static final int STATE_CONNECTED = 3;  // now connected to a remote device

	// Message types sent from the BluetoothService Handler
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_CONNECTED = 4;
	public static final int MESSAGE_DISCONNECTED = 5;

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 *
	 * @param handler A Handler to send messages back to the UI Activity
	 */
	public BluetoothService(Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * Set the current state of the chat connection
	 *
	 * @param state An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void startServer() {
		if (D)
			Log.d(TAG, "start");

		// cancel all running threads
		stop();

		setState(STATE_LISTEN);

		// Start the thread to listen on a BluetoothServerSocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 *
	 * @param device The BluetoothDevice to connect
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connect to: " + device.getName());

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}


	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 *
	 * @param socket The BluetoothSocket on which the connection was made
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connected");

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		mHandler.obtainMessage(BluetoothService.MESSAGE_CONNECTED).sendToTarget();
		mHandler.obtainMessage(BluetoothService.MESSAGE_CONNECTED, -1, -1, device).sendToTarget();

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		Log.d(TAG, "stop");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 *
	 * @param out The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionFailed() {
		Log.d(TAG, "connection lost");
		// Send a failure message back to the Activity
		mHandler.obtainMessage(BluetoothService.MESSAGE_DISCONNECTED).sendToTarget();
		stop();
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted
	 * (or until cancelled).
	 */
	private class AcceptThread extends Thread {
		// The local server socket
		private BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;

			// Create a new listening server socket
			try {
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
				try {
					mmServerSocket.close();
					mmServerSocket = null;
				} catch (IOException e1) {
					Log.e(TAG, e1.toString());
				}
				connectionFailed();
			}
			mmServerSocket = tmp;
		}

		public void run() {
			if (D)
				Log.d(TAG, "BEGIN mAcceptThread" + this);
			setName("AcceptThread");

			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (Exception e) {
					try {
						socket.close();
					} catch (Exception e1) {
						Log.e(TAG, e1.toString());
					}
					connectionFailed();
					Log.e(TAG, "accept() failed", e);
					break;
				}

				synchronized (BluetoothService.this) {
					switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
					}
				}
			}
			if (D)
				Log.i(TAG, "END mAcceptThread");

		}

		public void cancel() {
			Log.d(TAG, "cancel " + this);
			try {
				mmServerSocket.close();
				mmServerSocket = null;
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			} catch (NullPointerException e) {
				Log.d(TAG, "Null pointer", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection
	 * with a device. It runs straight through; the connection either
	 * succeeds or fails.
	 */
	private class ConnectThread extends Thread {
		private BluetoothSocket mmSocket;
		private BluetoothDevice device;

		public ConnectThread(BluetoothDevice device) {
			this.device = device;
			BluetoothSocket tmp = null;
			Log.d(TAG, "connect thread constructor");
			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				Log.d(TAG, "connect thread create rf comm socket");
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				connectionFailed();
				Log.e(TAG, "create() failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				Log.d(TAG, "trying to connect");
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					Log.d(TAG, "io exception, close");
					mmSocket.close();
					mmSocket = null;
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() socket during connection failure", e2);
				}
				e.printStackTrace();
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, device);
		}

		public void cancel() {
			Log.d(TAG, "canceling connect thread");
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private BluetoothSocket mmSocket;
		private InputStream mmInStream;
		private OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				connectionFailed();
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
					ObjectInputStream in = new ObjectInputStream(bis);
					Object object = in.readObject();
					in.close();

					Log.d(TAG, "got a message");
					// Send the obtained bytes to the UI Activity
					mHandler.obtainMessage(BluetoothService.MESSAGE_READ, bytes, -1, object).sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionFailed();
					break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					connectionFailed();
					Log.e(TAG, e.toString());
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 *
		 * @param buffer The bytes to write
		 */
		synchronized public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);

				// Share the sent message back to the UI Activity
				mHandler.obtainMessage(BluetoothService.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			Log.d(TAG, "canceling connected thread");
			try {
				mmInStream.close();
				mmInStream = null;
			} catch (Exception e) {}
			try {
				mmOutStream.close();
				mmOutStream = null;
			} catch (Exception e) {}
			try {
				mmSocket.close();
				mmSocket = null;
			} catch (Exception e) {}
		}
	}

	public static byte[] objectToByteArray(Object object) {
		// send over bluetooth
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(object);
			out.reset();
			out.close();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return null;
	}
}
