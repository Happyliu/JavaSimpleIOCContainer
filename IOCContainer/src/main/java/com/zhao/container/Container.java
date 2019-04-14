package com.zhao.container;


import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Constructor;

public class Container {

    Map<Class, Registration> registrations;
    Map<Class, Converter> converters=new HashMap<>();

    interface Converter<T>{
        //delegate the function with the parameter string
        T convert(String valueAsString);
    }

    public Container(String configurationPath) throws IOCException{
        File file=new File(configurationPath);
        if(!file.exists()){
            throw new IOCException(new FileNotFoundException());
        }

        Loader loader=new Loader();
        registrations=loader.loadConfiguration(configurationPath);
        registerConverters();
    }

    public <T> T resolve(Class<T> type) throws IOCException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Registration registration = registrations.get(type);
        T instance;
        List<com.zhao.container.Constructor> constructorParams = registration.getConstructorParams();
        try{
            Class cls=Class.forName(registration.getMapTo());
            //get constructor
            Constructor longestConstructor = getLongestConstructor(cls);
            //resolve the object
            Parameter[] parameters=longestConstructor.getParameters();
            //get all param instances
            List<Object> paramInstances=new ArrayList<>();
            for(Parameter parameter : parameters){
                Class paramClass=parameter.getType();
                if(paramClass.isPrimitive()||paramClass.isAssignableFrom(String.class)){
                    getNonReferenceParameters(constructorParams,paramInstances,parameter,paramClass);
                }else{
                    getConfigureParameters(paramInstances,paramClass);
                }
            }
            //create instance
            instance = createInstance(longestConstructor,paramInstances);
        } catch (ClassNotFoundException e) {
            throw new IOCException(e);
        }
        return instance;
    }

    private <T> T createInstance(Constructor longestConstructor, List<Object> paramInstances) throws InstantiationException, InvocationTargetException, IllegalAccessException {
        T instance;
        Parameter[] parameterTypes=longestConstructor.getParameters();
        Object[] paramArray=new Object[parameterTypes.length];
        for(int i=0;i<paramArray.length;i++){
            //need to use the getType for the parameterTypes array to get the decalared type
            //not the the runtime type, if the use getClass it will return the runtime type which is
            //the reflection parameter
            Class paramClass=parameterTypes[i].getType();
            Class argumentClass=paramInstances.get(i).getClass();
            if(paramClass.isPrimitive()||argumentClass.isPrimitive()){
                if(primitivesMatch(argumentClass,paramClass)){
                    paramArray[i]=paramInstances.get(i);
                }
            }
            //assign instance to its interface parameter in the construtor
            if(paramClass.isAssignableFrom(argumentClass)){
                paramArray[i]=paramInstances.get(i);
            }
        }
        instance=(T)longestConstructor.newInstance(paramArray);

        return instance;
    }

    private boolean primitivesMatch(Class argumentClass, Class parameterClass) {
        if ((argumentClass == int.class || argumentClass == Integer.class) && (parameterClass == int.class || parameterClass == Integer.class)) {
            return true;
        }
        if ((argumentClass == byte.class || argumentClass == Byte.class) && (parameterClass == byte.class || parameterClass == Byte.class)) {
            return true;
        }
        if ((argumentClass == short.class || argumentClass == Short.class) && (parameterClass == short.class || parameterClass == Short.class)) {
            return true;
        }
        if ((argumentClass == long.class || argumentClass == Long.class) && (parameterClass == long.class || parameterClass == Long.class)) {
            return true;
        }
        if ((argumentClass == char.class || argumentClass == Character.class) && (parameterClass == char.class || parameterClass == Character.class)) {
            return true;
        }
        if ((argumentClass == double.class || argumentClass == Double.class) && (parameterClass == double.class || parameterClass == Double.class)) {
            return true;
        }
        if ((argumentClass == float.class || argumentClass == Float.class) && (parameterClass == float.class || parameterClass == Float.class)) {
            return true;
        }
        if ((argumentClass == boolean.class || argumentClass == Boolean.class) && (parameterClass == boolean.class || parameterClass == Boolean.class)) {
            return true;
        }
        if ((argumentClass == int.class || argumentClass == Integer.class) && (parameterClass == int.class || parameterClass == Integer.class)) {
            return true;
        }
        return false;
    }

    private void getConfigureParameters(List<Object> parameterInstances, Class parameterClass) throws IOCException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Object resolvedInstance = resolve(parameterClass);
        parameterInstances.add(resolvedInstance);
    }

    private void getNonReferenceParameters(List<com.zhao.container.Constructor> constructorParams, List<Object> paramInstance,Parameter parameter,Class paramClass){
        Object value=null;
        for(com.zhao.container.Constructor constructor : constructorParams){
            if(constructor.getName().equals(parameter.getName())){
                value=constructor.getValue();
                break;
            }
        }
        Converter c=converters.get(paramClass);
        paramInstance.add(c.convert(value.toString()));
    }

    private Constructor getLongestConstructor(Class cls){
        Constructor[] constructors=cls.getConstructors();
        Constructor longestConstructor=constructors[0];
        for(Constructor constructor : constructors){
            if(constructor.getParameterCount() > longestConstructor.getParameterCount()){
                longestConstructor=constructor;
            }
        }
        return longestConstructor;
    }

    private void registerConverters(){
        converters.put(int.class, Integer::parseInt);
        converters.put(float.class, Float::parseFloat);
        converters.put(double.class, Double::parseDouble);
        converters.put(byte.class, Byte::parseByte);
        converters.put(long.class, Long::parseLong);
        converters.put(short.class, Short::parseShort);
        converters.put(boolean.class, Boolean::parseBoolean);
        converters.put(String.class, s -> s);
        converters.put(Character.class, c -> c);
    }

}
