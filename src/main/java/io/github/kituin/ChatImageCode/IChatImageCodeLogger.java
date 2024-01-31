package io.github.kituin.ChatImageCode;

public interface IChatImageCodeLogger {
    /**
     * 打印日志
     * @param log 日志内容
     */
    void info(String log);
    /**
     * 打印日志
     * @param log 日志内容
     * @param args 参数
     */
    void info(String log, Object... args);

    /**
     * 打印日志
     * @param log 日志内容
     */
    void debug(String log);
    /**
     * 打印日志
     * @param log 日志内容
     * @param args 参数
     */
    void debug(String log, Object... args);

    /**
     * 打印日志
     * @param log 日志内容
     */
    void error(String log);
    /**
     * 打印日志
     * @param log 日志内容
     * @param args 参数
     */
    void error(String log, Object... args);
}
