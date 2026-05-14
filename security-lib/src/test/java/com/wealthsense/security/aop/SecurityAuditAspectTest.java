package com.wealthsense.security.aop;

import com.wealthsense.common.util.CorrelationIdUtil;
import com.wealthsense.security.audit.AuditLog;
import com.wealthsense.security.audit.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityAuditAspectTest {

    @Mock
    private AuditService auditService;

    private SecurityAuditAspect aspect;

    @BeforeEach
    void setUp() {
        this.aspect = new SecurityAuditAspect(auditService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        CorrelationIdUtil.clearCorrelationId();
        RequestContextHolder.resetRequestAttributes();
    }

    static class TestMethods {

        @Auditable(action = "CREATE", resource = "TRANSACTION")
        public String ok(UUID resourceId) {
            return "OK";
        }

        @Auditable(action = "LOGIN", resource = "AUTH")
        public String fail(UUID resourceId) {
            throw new RuntimeException("Invalid password=supersecret");
        }
    }

    @Test
    void audit_successfulMethod_logsSuccess() throws Throwable {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        CorrelationIdUtil.setCorrelationId("corr-123");

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("127.0.0.1");
        req.addHeader("User-Agent", "JUnit");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getArgs()).thenReturn(new Object[]{resourceId});
        when(pjp.proceed()).thenReturn("OK");

        Method m = TestMethods.class.getMethod("ok", UUID.class);
        Auditable ann = m.getAnnotation(Auditable.class);

        Object result = aspect.audit(pjp, ann);
        assertEquals("OK", result);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditService).save(captor.capture());
        AuditLog log = captor.getValue();

        assertEquals(userId, log.getUserId());
        assertEquals("CREATE", log.getAction());
        assertEquals("TRANSACTION", log.getResource());
        assertEquals(resourceId.toString(), log.getResourceId());
        assertTrue(log.isSuccess());
        assertNull(log.getErrorMessage());
        assertEquals("corr-123", log.getCorrelationId());
    }

    @Test
    void audit_failedMethod_logsFailure() throws Throwable {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        CorrelationIdUtil.setCorrelationId("corr-456");

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.1");
        req.addHeader("User-Agent", "JUnit");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getArgs()).thenReturn(new Object[]{resourceId});
        when(pjp.proceed()).thenThrow(new RuntimeException("Invalid password=supersecret"));

        Method m = TestMethods.class.getMethod("fail", UUID.class);
        Auditable ann = m.getAnnotation(Auditable.class);

        assertThrows(RuntimeException.class, () -> aspect.audit(pjp, ann));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditService).save(captor.capture());
        AuditLog log = captor.getValue();

        assertEquals("LOGIN", log.getAction());
        assertEquals("AUTH", log.getResource());
        assertFalse(log.isSuccess());
        assertNotNull(log.getErrorMessage());
        assertFalse(log.getErrorMessage().toLowerCase().contains("password"));
        assertEquals("corr-456", log.getCorrelationId());
    }

    @Test
    void audit_measuresExecutionTime() throws Throwable {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        CorrelationIdUtil.setCorrelationId("corr-789");

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("127.0.0.1");
        req.addHeader("User-Agent", "JUnit");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getArgs()).thenReturn(new Object[]{resourceId});
        when(pjp.proceed()).thenAnswer(inv -> {
            Thread.sleep(5);
            return "OK";
        });

        Method m = TestMethods.class.getMethod("ok", UUID.class);
        Auditable ann = m.getAnnotation(Auditable.class);

        aspect.audit(pjp, ann);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditService).save(captor.capture());
        AuditLog log = captor.getValue();

        assertNotNull(log.getDurationMs());
        assertTrue(log.getDurationMs() >= 1);
    }
}

