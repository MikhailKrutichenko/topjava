package ru.javawebinar.topjava;

import org.junit.rules.ExternalResource;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TimingRules {
    private static final Logger log = LoggerFactory.getLogger(TimingRules.class);

    private static final StringBuilder results = new StringBuilder();

    private static final String DELIM = new String(new char[103]).replace("\0", "-");

    public static final Stopwatch STOPWATCH = new Stopwatch() {
        @Override
        protected void finished(long nanos, Description description) {
            String result = String.format("%-95s %7d", description.getDisplayName(), TimeUnit.NANOSECONDS.toMillis(nanos));
            results.append(result).append('\n');
            log.info(result + " ms\n");
        }
    };

    public static final ExternalResource SUMMARY = new ExternalResource() {

        @Override
        protected void before() {
            results.setLength(0);
        }

        @Override
        protected void after() {
            log.info("\n" + DELIM +
                    "\nTest                                                                                       Duration, ms" +
                    "\n" + DELIM + "\n" + results + DELIM + "\n");
        }
    };
}
