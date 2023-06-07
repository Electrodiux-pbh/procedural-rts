package com.electrodiux;

public final class Time {

    private static float deltaTime = 0.0f;
    private static float fixedDeltaTime = 0.0f;
    private static int updateCount = 0;

    private static double startTime;
    private static double time;

    // #region Get Values

    public static float realtimeSinceStartup() {
        return (float) (milisecondsToSeconds(System.currentTimeMillis()) - startTime);
    }

    public static double realtimeSinceStartupAsDouble() {
        return milisecondsToSeconds(System.currentTimeMillis()) - startTime;
    }

    public static float time() {
        return (float) time;
    }

    public static double timeAsDouble() {
        return time;
    }

    public static float deltaTime() {
        return deltaTime;
    }

    public static float fixedDeltaTime() {
        return fixedDeltaTime;
    }

    public static int updateCount() {
        return updateCount;
    }

    // #endregion

    // #region Update values

    static void startTime() {
        startTime = milisecondsToSeconds(System.currentTimeMillis());
        updateTime();
    }

    static void updateTime() {
        time = milisecondsToSeconds(System.currentTimeMillis()) - startTime;
    }

    static void increaseUpdateCount() {
        updateCount++;
    }

    static void setDeltaTime(float delta) {
        deltaTime = delta;
    }

    static void setFixedDeltaTime(float delta) {
        fixedDeltaTime = delta;
    }

    // #endregion

    private static double milisecondsToSeconds(long miliseconds) {
        return miliseconds / 1000.0D;
    }

}
