package io.renren.modules.test.jmeter;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.jmeter.rmi.RmiUtils;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.RemoteThreadsListener;
import org.apache.jmeter.util.JMeterUtils;

public class RemoteThreadsListenerTest extends UnicastRemoteObject implements RemoteThreadsListener, ThreadListener {

	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_LOCAL_PORT = JMeterUtils.getPropDefault("client.rmi.localport", 0);
	private int threadNumber;
	
	public RemoteThreadsListenerTest() throws RemoteException {
		super(DEFAULT_LOCAL_PORT, RmiUtils.createClientSocketFactory(), RmiUtils.createServerSocketFactory());
		this.threadNumber = 0;
	}
	
	@Override
	public void threadStarted() {
		threadNumber++;
    }
	
	@Override
    public void threadFinished() {
		threadNumber--;        
    }
	
	public int getThreadNumber() { return threadNumber;}
}
