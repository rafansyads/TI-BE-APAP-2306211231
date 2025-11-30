package apap.ti._5.accommodation_2306211231_be.testutils;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.lang.reflect.Field;
import java.util.Arrays;

public class AutoMockExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        // Make the SecurityContext inheritable by forked/mock threads (helps MockMvc)
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        // Set a safe SecurityContext with a TestUser principal so controllers won't crash
        TestUser principal = new TestUser("test-user");
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext sc = new SecurityContextImpl();
        sc.setAuthentication(auth);
        SecurityContextHolder.setContext(sc);

        // For each field in the test instance, if it's a restservice/service type, ensure an instance
        // exists and that its internal collaborator fields are mocked to avoid NPEs.
        Field[] fields = testInstance.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            Object value = f.get(testInstance);
            Class<?> fieldType = f.getType();
            String fieldPkg = fieldType.getPackage() != null ? fieldType.getPackage().getName() : "";

            if (fieldPkg.contains("restservice") || fieldPkg.contains("service")) {
                // If the test didn't create the service instance, create a spy that calls real methods
                if (value == null) {
                    try {
                        Object spy = Mockito.mock(fieldType, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
                        f.set(testInstance, spy);
                        value = spy;
                    } catch (Exception ex) {
                        // fallback to a plain mock if spy creation fails
                        Object mock = Mockito.mock(fieldType);
                        f.set(testInstance, mock);
                        value = mock;
                    }
                }

                // Inspect fields declared on the service type (not the proxy class) and mock null collaborators
                for (Field sf : fieldType.getDeclaredFields()) {
                    try {
                        // Try to locate the corresponding Field on the runtime instance (handles proxies/spy subclasses)
                        Field realField = findFieldInHierarchy(value.getClass(), sf.getName());
                        if (realField == null) {
                            // nothing we can do
                            continue;
                        }
                        realField.setAccessible(true);
                        Object val = realField.get(value);
                        if (val == null) {
                            Class<?> t = sf.getType();
                            Object mock = Mockito.mock(t);
                            realField.set(value, mock);
                        }
                    } catch (IllegalAccessException e) {
                        // ignore
                    }
                }
            }
        }
    }

    private Field findFieldInHierarchy(Class<?> cls, String name) {
        Class<?> cur = cls;
        while (cur != null && cur != Object.class) {
            try {
                Field f = cur.getDeclaredField(name);
                return f;
            } catch (NoSuchFieldException e) {
                cur = cur.getSuperclass();
            }
        }
        return null;
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        SecurityContextHolder.clearContext();
    }
}
