package com.electrodiux.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventManager {

    private Map<Class<? extends Event>, List<Method>> eventListeners;
    private Map<Class<? extends Event>, List<Listener>> eventListenerObjects;

    private Queue<Event> eventQueue;

    public EventManager() {
        eventListeners = new HashMap<>();
        eventListenerObjects = new HashMap<>();
        eventQueue = new ConcurrentLinkedQueue<>();
    }

    public void registerListener(Listener listener) {
        Class<?> clazz = listener.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length != 1) {
                    throw new IllegalArgumentException("The method " + method.getName() + " in class " + clazz.getName()
                            + " has an invalid number of parameters. Expected 1, got " + parameters.length);
                }

                Class<?> parameterClass = parameters[0];
                System.out.println(parameterClass.getName());
                if (!Event.class.isAssignableFrom(parameterClass)) {
                    throw new IllegalArgumentException("The method " + method.getName() + " in class " + clazz.getName()
                            + " has an invalid parameter type. Expected a subclass of Event, got "
                            + parameterClass.getName());
                }

                method.setAccessible(true);

                Class<? extends Event> eventClass = parameterClass.asSubclass(Event.class);
                eventListeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(method);
                eventListenerObjects.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
            }
        }
    }

    public void unregisterListener(Listener listener) {
        Class<?> clazz = listener.getClass();
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                Class<? extends Event> eventClass = method.getParameterTypes()[0].asSubclass(Event.class);
                List<Method> listeners = eventListeners.get(eventClass);
                List<Listener> listenerObjects = eventListenerObjects.get(eventClass);

                if (listeners != null) {
                    listeners.remove(method);
                    if (listeners.isEmpty()) {
                        eventListeners.remove(eventClass);
                    }
                }

                if (listenerObjects != null) {
                    listenerObjects.remove(listener);
                    if (listenerObjects.isEmpty()) {
                        eventListenerObjects.remove(eventClass);
                    }
                }
            }
        }
    }

    public void dispatchEvents() {
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            List<Method> methods = eventListeners.get(event.getClass());
            List<Listener> listeners = eventListenerObjects.get(event.getClass());

            if (methods != null && listeners != null) {
                Iterator<Method> methodsIter = methods.iterator();
                Iterator<Listener> listenersIter = eventListenerObjects.get(event.getClass()).iterator();

                while (methodsIter.hasNext() && listenersIter.hasNext()) {
                    Method method = methodsIter.next();
                    Listener listener = listenersIter.next();

                    try {
                        method.invoke(listener, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void fireEvent(Event event) {
        eventQueue.add(event);
    }

}
