package me.hexett.staffUtilsPlus.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe service registry for managing plugin services.
 * Provides a centralized way to register and retrieve service implementations.
 * 
 * @author Hexett
 */
public class ServiceRegistry {

    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    /**
     * Register a service implementation.
     * 
     * @param <T> The service type
     * @param clazz The service class
     * @param implementation The service implementation
     */
    public static <T> void register(Class<T> clazz, T implementation) {
        if (clazz == null) {
            throw new IllegalArgumentException("Service class cannot be null");
        }
        if (implementation == null) {
            throw new IllegalArgumentException("Service implementation cannot be null");
        }
        services.put(clazz, implementation);
    }

    /**
     * Retrieve a service implementation.
     * 
     * @param <T> The service type
     * @param clazz The service class
     * @return The service implementation, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Service class cannot be null");
        }
        return (T) services.get(clazz);
    }

    /**
     * Check if a service is registered.
     * 
     * @param clazz The service class
     * @return true if the service is registered, false otherwise
     */
    public static boolean isRegistered(Class<?> clazz) {
        return clazz != null && services.containsKey(clazz);
    }

    /**
     * Unregister a service.
     * 
     * @param clazz The service class to unregister
     * @return The removed service implementation, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T unregister(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        return (T) services.remove(clazz);
    }

    /**
     * Clear all registered services.
     */
    public static void clear() {
        services.clear();
    }
}
