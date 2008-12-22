/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.json.MapByClass;


import static org.apache.commons.lang.StringUtils.*;

/**
 * Uses reflection to trace to find the property value.
 * if at any point a null is returned then null is returned (no {@link NullPointerException} will be thrown)
 * @author patmoore
 *
 */
public class ReflectionFlowPropertyValueProvider implements FlowPropertyValueProvider<FlowActivity> {

    private Object object;
    private String[] propertyNames;
    private MapByClass<Method[]> methods = new MapByClass<Method[]>();
    public ReflectionFlowPropertyValueProvider(Object object) {
        this.object = object;
    }
    public ReflectionFlowPropertyValueProvider(String... propertyNames) {
        this.propertyNames = propertyNames;
    }

    public ReflectionFlowPropertyValueProvider(Class<?>clazz, String... propertyNames) {
        this.propertyNames = propertyNames;
        this.methods.put(clazz, getMethods(clazz, propertyNames));
    }
    public ReflectionFlowPropertyValueProvider(Object object, String... propertyNames) {
        this(object);
        this.propertyNames = propertyNames;
        Class<? extends Object> clazz = object.getClass();
        this.methods.put(clazz, getMethods(clazz, propertyNames));
    }

    private Method[] getMethods(Class<?> clazz, String... propertyNamesList) {
        int i = 0;
        Class<?>[] parameterTypes = new Class<?>[0];
        Method[] methodArray = new Method[propertyNamesList.length];
        for(String propertyName: propertyNamesList) {
            methodArray[i] = getMethod(clazz, propertyName, parameterTypes);
            clazz= methodArray[i].getReturnType();
            i++;
        }
        return methodArray;
    }
    /**
     * @param clazz
     * @param propertyName
     * @param parameterTypes
     */
    private Method getMethod(Class<?> clazz, String propertyName, Class<?>... parameterTypes) {
        if (propertyName == null ) {
            throw new IllegalArgumentException("propertyName cannot be null");
        }
        for (String methodName: Arrays.asList(propertyName, "get"+capitalize(propertyName), "is"+capitalize(propertyName))) {
            try {
                return clazz.getMethod(methodName, parameterTypes);
            } catch (SecurityException e) {
//                throw new IllegalArgumentException(clazz+"."+propertyName+ " " + StringUtils.join(parameterTypes), e);
            } catch (NoSuchMethodException e) {
//                throw new IllegalArgumentException(clazz+"."+propertyName+ " " + StringUtils.join(parameterTypes), e);
            }
        }
        throw new IllegalArgumentException(clazz+"."+propertyName+ " " + join(parameterTypes));
    }
    /**
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        final Object base = this.object==null?flowActivity:this.object;
        Class<? extends Object> clazz = base.getClass();
        Method[] methodArray = this.methods.get(clazz);
        if ( methodArray == null ) {
            methodArray = getMethods(base.getClass(), propertyNames);
            this.methods.put(base.getClass(), methodArray);
        }
        Object result = base;
        if ( methodArray != null && result != null) {
            for(Method method : methodArray) {
                try {
                    result = method.invoke(result);
                    if ( result == null ) {
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(method.toGenericString(), e);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(method.toGenericString(), e);
                } catch (InvocationTargetException e) {
                    throw new IllegalArgumentException(method.toGenericString(), e);
                }
            }
        }
        return (T) result;
    }

}
