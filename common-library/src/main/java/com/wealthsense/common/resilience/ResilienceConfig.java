package com.wealthsense.common.resilience;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Global Resilience4J configuration.
 *
 * <p>All circuit-breaker instances are driven by application.yml. This class
 * simply ensures AOP proxying is active.</p>
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ResilienceConfig {
}
