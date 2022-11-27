package com.electrodiux.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class uses to make an internal timer in new thread
 * 
 * @author Electrodiux
 *
 */
public class Timer {

	private static int lastTimerId = 1;

	private volatile float fpsValue = 0;
	private short timerFps = 0;

	private boolean running = false;
	private boolean stopOnException = false;
	private List<TimerHandler> handlers = new ArrayList<>();
	private Thread thread = null;

	private volatile float deltaTime = 0;
	private volatile float timeReference = 0;
	private volatile float delta = 0;
	private volatile long updateReference = 0;
	private volatile long resetReference = 0;

	/**
	 * Creates a new Timer object and synchronize the timer to 60fps calling method
	 * sync
	 */
	public Timer() {
		sync(60);

		lastTimerId++;
	}

	/**
	 * Creates a new Timer object and synchronize the timer to the specific fps
	 * calling {@link #sync(int)}
	 */
	public Timer(int fps) {
		this();
		sync(fps);
	}

	/**
	 * Creates a new Timer object and synchronize the timer to the specific fps
	 * calling {@link #sync(int)} and
	 * adds a default handler calling {@link #addHandler(TimerHandler)}
	 */
	public Timer(TimerHandler handler, int fps) {
		this(fps);
		addHandler(handler);
	}

	/**
	 * Creates a new Timer object with a default handler
	 */
	public Timer(TimerHandler handler) {
		this();
		addHandler(handler);
	}

	/**
	 * This method start the timer counts calling {@link Timer#start(boolean)} and
	 * giving
	 * for params true
	 */
	public void start() {
		start(true);
	}

	/**
	 * This method start the timer counts, if is allredy initialized the method will
	 * return, and do nothing.
	 * and if you give for params true, the execution will start on a new thread, in
	 * case you
	 * give a false, the method start on the current thread.
	 * 
	 * @param newThread
	 */
	public void start(boolean newThread) {
		if (running)
			return;
		running = true;

		if (newThread) {
			thread = new Thread(getClass().getName() + ": " + lastTimerId) {

				public void run() {
					runTimer();
				}

			};
			thread.start();
		} else {
			runTimer();
		}
	}

	/**
	 * This method it's called when it's time to call all handlers
	 */
	protected void update() {
		for (TimerHandler handler : handlers) {
			try {
				handler.update();
			} catch (Exception e) {
				if (stopOnException)
					stop();
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method calls to resetDeltas(), to reset deltas, and stop running while.
	 * If a proccess not finished, it will finished
	 * before the stop thread was closed
	 */
	public void stop() {
		resetDeltas();
		running = false;
	}

	/**
	 * This method resets all delta values, for restart timer calculations
	 */
	public void resetDeltas() {
		deltaTime = 0;
		timeReference = 0;
		delta = 0;
		updateReference = System.currentTimeMillis();
		resetReference = System.currentTimeMillis() + 1000; // set current time and adds 1000 milliseconds
	}

	/**
	 * This method synchronized dynamically, the fps of your timer. How much times
	 * per second calls method update();
	 * 
	 * @param fps to sync
	 */
	public void sync(float fps) {
		if (fps <= 0)
			return;
		fpsValue = 1000F / fps;
	}

	/**
	 * This method add a TimerHandler
	 * 
	 * @param handler to introduce
	 */
	public void addHandler(TimerHandler handler) {
		handlers.add(handler);
	}

	/**
	 * This method removed a TimerHandler
	 * 
	 * @param handler to remove
	 */
	public void removeHandler(TimerHandler handler) {
		handlers.add(handler);
	}

	/**
	 * This method returns the value of delta time, this is how much time, pass in
	 * to last fps and this fps. Can used to
	 * calculate cooldowns. That example show how much blocks you need move that
	 * second to move the blocksPerSecond sync.
	 * <p>
	 * int blocksPerSecond = 5;<br>
	 * <br>
	 * update() {<br>
	 * move(blocksPerSecond * timer.getDeltaTime());<br>
	 * }<br>
	 * </p>
	 * 
	 * 
	 * @return deltaTime value
	 */
	public float getDeltaTime() {
		return deltaTime;
	}

	/**
	 * Return the times the timer updates in the last second
	 * 
	 * @return
	 */
	public int getFps() {
		return timerFps;
	}

	/**
	 * This method set the value of property "stopOnException", In case is true:
	 * when in some thread throws a uncaught exeception, the timer
	 * stop and throw the exception. In case is flase: only catch and show in
	 * console the exception.
	 */
	public void setStopOnException(boolean value) {
		this.stopOnException = value;
	}

	/**
	 * This method return, the value of property "stopOnException"
	 * 
	 * @return the value of property "stopOnException"
	 */
	public boolean stopOnException() {
		return stopOnException;
	}

	/**
	 * The default run method
	 */
	private void runTimer() {

		resetDeltas();

		long startWhile = 0;
		short fpsCounter = 0;

		while (running) {
			startWhile = System.currentTimeMillis();

			timeReference = startWhile - updateReference;
			updateReference = startWhile;

			delta += timeReference / fpsValue;

			while (delta >= 1) {
				update();
				fpsCounter++;
				delta = 0;
			}

			if (resetReference <= System.currentTimeMillis()) {
				resetReference = System.currentTimeMillis() + 1000; // set current time and adds 1000 milliseconds
				timerFps = fpsCounter;
				deltaTime = 1F / getFps();
				fpsCounter = 0;
			}
		}
	}

	private static Timer defaultTimer = new Timer();

	/**
	 * This method return the current system time on seconds with:
	 * ({@link java.lang.System#nanoTime()} * 1E-9) on float value
	 * it uses to graphics, animation, and general sync
	 * 
	 * @return the time value
	 */
	public static float getTime() {
		return (float) (System.nanoTime() * 1E-9);
	}

	public static Timer getDefaultTimer() {
		return defaultTimer;
	}

	public static float deltaTime() {
		return getDefaultTimer().getDeltaTime();
	}

}
