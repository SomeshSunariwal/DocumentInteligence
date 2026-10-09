package com.example.doc_intel.DocumentEncoder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Silences only the named encoder during a test that deliberately triggers an error. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(SuppressEncoderLogs.Extension.class)
@interface SuppressEncoderLogs {

    Class<?> value();

    class Extension implements BeforeEachCallback, AfterEachCallback {

        private Logger logger;

        private Level previousLevel;

        @Override
        public void beforeEach(ExtensionContext context) {
            Class<?> encoder = context.getRequiredTestMethod().getAnnotation(SuppressEncoderLogs.class).value();
            logger = (Logger) LoggerFactory.getLogger(encoder);
            previousLevel = logger.getLevel();
            logger.setLevel(Level.OFF);
        }

        @Override
        public void afterEach(ExtensionContext context) {
            // Restore logging even if an assertion fails.
            logger.setLevel(previousLevel);
        }
    }
}
