package sample;

import java.io.*;

class TouchInput implements Runnable {
	private InputStream is;
	private String type;
	private File f;
	private BufferedWriter out;

	TouchInput(InputStream is, String type, File f) {
		this.is = is;
		this.type = type;
		this.f = f;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String s = null;
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f.getAbsolutePath(), true)));
			while ((s = in.readLine()) != null) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
//				System.out.println(s);
				out.write(s + '\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

public class Touchevent implements Runnable {
	private volatile Thread threadEvent;
	private volatile Thread threadInput;
	private volatile Thread threadError;
	private Controller c;
	private File f;

	public Touchevent(Controller c) {
		threadEvent = null;
		threadInput = null;
		threadError = null;
		this.c = c;
	}

	public void run() {
		try {
			Process process = Runtime.getRuntime().exec("adb shell getevent -lt");
			threadInput = new Thread(new TouchInput(process.getInputStream(), "Info", f));
			threadError = new Thread(new TouchInput(process.getErrorStream(), "Error", f));
			threadInput.start();
			threadError.start();
			int value = process.waitFor();
			c.showDraw("adb连接已断开！");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
//			interrupted
		} finally {
			if (threadInput != null) {
				threadInput.interrupt();
				threadInput = null;
			}
			if (threadError != null) {
				threadError.interrupt();
				threadError = null;
			}
		}
	}

	public void start() {
		if (threadEvent == null) {
			String tag = c.stageList.get(c.crtStage).tag;
			tag = tag.replace(' ', '_');
			f = new File("data/TouchData/" + "Task_" + tag + "_" + System.currentTimeMillis() + ".txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			threadEvent = new Thread(this, "Touchevent");
			threadEvent.start();
		}
	}

	public void stop() {
		if (threadEvent != null) {
			threadEvent.interrupt();
			threadEvent = null;
		}
	}
}
