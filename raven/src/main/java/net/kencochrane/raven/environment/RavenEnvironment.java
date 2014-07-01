package net.kencochrane.raven.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages environment information on Raven.
 * <p>
 * Manages information related to Raven Runtime such as the name of the library or
 * whether or not the thread is managed by Raven.
 * </p>
 */
public final class RavenEnvironment {
    /**
     * Version of this client, the major version is the current supported Sentry protocol, the minor version changes
     * for each release of this project.
     */
    public static final String NAME = ResourceBundle.getBundle("raven-build").getString("build.name");
    /**
     * Indicates whether the current thread is managed by raven or not.
     */
    protected static final ThreadLocal<AtomicInteger> RAVEN_THREAD = new ThreadLocal<AtomicInteger>() {
        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };
    private static final Logger logger = LoggerFactory.getLogger(RavenEnvironment.class);

    private RavenEnvironment(){
    }

    /**
     * Sets the current thread as managed by Raven.
     * <p>
     * The logs generated by Threads managed by Raven will not send logs to Sentry.
     * </p>
     * <p>
     * Recommended usage:
     * <pre>{@code
     * RavenEnvironment.startManagingThread();
     * try {
     *     // Some code that shouldn't generate Sentry logs.
     * } finally {
     *     RavenEnvironment.stopManagingThread();
     * }
     * }</pre>
     * </p>
     */
    public static void startManagingThread() {
        try {
            if (isManagingThread())
                logger.warn("Thread already managed by Raven");
        } finally {
            RAVEN_THREAD.get().incrementAndGet();
        }
    }

    /**
     * Sets the current thread as not managed by Raven.
     * <p>
     * The logs generated by Threads not managed by Raven will send logs to Sentry.
     * </p>
     */
    public static void stopManagingThread() {
        try {
            if (!isManagingThread()) {
                //Start managing the thread only to send the warning
                startManagingThread();
                logger.warn("Thread not yet managed by Raven");
            }
        } finally {
            RAVEN_THREAD.get().decrementAndGet();
        }
    }

    /**
     * Checks whether the current thread is managed by Raven or not.
     *
     * @return {@code true} if the thread is managed by Raven, {@code false} otherwise.
     */
    public static boolean isManagingThread() {
        return RAVEN_THREAD.get().get() > 0;
    }
}
