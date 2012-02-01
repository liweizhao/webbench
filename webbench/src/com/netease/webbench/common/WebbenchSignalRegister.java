/**
 * Copyright (C) NetEase Corporation
 * Author: LWZH
 * Contact: liweizhao@163.org
 * Change Logs:
 * 
 */
package com.netease.webbench.common;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * 
 */
public class WebbenchSignalRegister  implements SignalHandler  {
	protected String oldSignalName;
	protected WebbenchSignalHandler handler;
	
	public WebbenchSignalRegister(WebbenchSignalHandler handler) {
		oldSignalName = null;
		this.handler = handler;
		installSigal();
	}

	/**
	 * register SIGINT signal
	 */
	private void installSigal() {
		//Signal termSignal = new Signal("TERM");
		//Signal.handle(termSignal, this);
		Signal intSignal = new Signal("INT");
		Signal.handle(intSignal, this);
	}

	/* (non-Javadoc)
	 * @see sun.misc.SignalHandler#handle()
	 */
	public void handle(Signal signal) {
		System.out.println("Signal handler called for signal " + signal);
		try {
			if (oldSignalName == null) { 
				oldSignalName = signal.getName();
				handler.signalAction(signal);
			} else {
				System.out.println("Last signal handler is processing, please wait.");
			}
		} catch (Exception e) {
			System.out.println("handle|Signal handler" + "failed, reason "
					+ e.getMessage());
			e.printStackTrace();
		}
	}
}
