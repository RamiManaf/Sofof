/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sofof.SofofException;

/**
 * Serialize objects to JSON in UTF-8. The class must have a default constructor
 * with no arguments. The supported types are primitives, String, arrays and
 * collections that can be written directly to JSON. Any other class will be
 * serialized by its fields. The serializer add a class field to objects so it
 * can deserialize them later. The serializer also add the class name to the
 * first element in arrays or collections so it can deserialize them later.
 *
 * @author Rami Manaf Abdullah
 */
public class JsonSerializer implements Serializer {

    private static final List<Class<?>> BOXING_TYPES = getBoxingTypes();

    @Override
    public String getName() {
        return "json";
    }

    @Override
    public byte[] serialize(Object obj) throws SofofException {
        Objects.nonNull(obj);
        return serializeData(obj).toString().getBytes(StandardCharsets.UTF_8);
    }

    private Object serializeData(Object obj) throws SofofException {
        try {
            Class c = (obj == null ? null : obj.getClass());
            if (obj == null) {
                return null;
            } else if (c.equals(char.class) || c.equals(Character.class)) {
                return String.valueOf(obj);
            } else if (c.isPrimitive() || isBoxingType(c)) {
                return obj;
            } else if (c.equals(String.class)) {
                return obj;
            } else if (c.isArray()) {
                JSONArray array = new JSONArray();
                array.put(c.getCanonicalName().substring(0, c.getCanonicalName().length() - 2));
                for (int i = 0; i < Array.getLength(obj); i++) {
                    array.put(serializeData(Array.get(obj, i)));
                }
                return array;
            } else if (Collection.class.isAssignableFrom(c)) {
                JSONArray array = new JSONArray();
                array.put(c.getCanonicalName().substring(0, c.getCanonicalName().length() - 2));
                Collection collection = (Collection) obj;
                collection.forEach((t) -> {
                    try {
                        array.put(serializeData(t));
                    } catch (SofofException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                return array;
            } else {
                return serializeObject(obj);
            }
        } catch (IOException | IllegalAccessException ex) {
            throw new SofofException(ex);
        }
    }

    private JSONObject serializeObject(Object obj) throws IOException, IllegalArgumentException, IllegalAccessException, SofofException {
        JSONObject jsonObj = new JSONObject();
        Class c = obj.getClass();
        jsonObj.put("class", c.getCanonicalName());
        do {
            List<Field> fields = getAllWritableFields(c);
            for (Field field : fields) {
                field.setAccessible(true);
                jsonObj.put(field.getName(), serializeData(field.get(obj)));
            }
        } while (!(c = c.getSuperclass()).equals(Object.class));
        return jsonObj;
    }

    public static List<Field> getAllWritableFields(Class<?> type) {
        return Arrays.asList(type.getDeclaredFields()).stream().filter((f) -> !Modifier.isTransient(f.getModifiers())
                && !Modifier.isStatic(f.getModifiers())
                && !Modifier.isFinal(f.getModifiers())).collect(Collectors.toList());
    }

    @Override
    public Object deserialize(byte[] bytes) throws SofofException {
        Object result = JSONObject.stringToValue(new String(bytes, StandardCharsets.UTF_8));
        if (result instanceof String) {
            String stringResult = (String) result;
            if (stringResult.startsWith("{")) {
                return deserializeData(new JSONObject(stringResult));
            } else if (stringResult.startsWith("[")) {
                return deserializeData(new JSONArray(stringResult));
            } else {
                return stringResult;
            }
        } else {
            return result;
        }
    }

    private Object deserializeData(Object obj) throws SofofException {
        try {
            if (obj instanceof JSONArray) {
                JSONArray jsonArray = ((JSONArray) obj);
                String className = jsonArray.getString(0);
                Class c;
                if (className.equals("int")) {
                    c = int.class;
                } else if (className.equals("short")) {
                    c = short.class;
                } else if (className.equals("long")) {
                    c = long.class;
                } else if (className.equals("byte")) {
                    c = byte.class;
                } else if (className.equals("float")) {
                    c = float.class;
                } else if (className.equals("double")) {
                    c = double.class;
                } else if (className.equals("boolean")) {
                    c = boolean.class;
                } else if (className.equals("char")) {
                    c = char.class;
                } else {
                    c = Class.forName(className);
                }
                int length = jsonArray.length() - 1;
                Object array = Array.newInstance(c, length);
                for (int i = 0; i < length; i++) {
                    Array.set(array, i, deserializeData(jsonArray.get(i + 1)));
                }
                return array;
            } else if (obj instanceof JSONObject) {
                return readObject((JSONObject) obj);
            } else {
                return obj;
            }
        } catch (ClassNotFoundException ex) {
            throw new SofofException(ex);
        }
    }

    private Object readObject(JSONObject jsonObj) throws SofofException {
        try {
            Class c = Class.forName(jsonObj.getString("class"));
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object obj = constructor.newInstance();
            do {
                List<Field> fields = getAllWritableFields(c);
                for (String key : jsonObj.keySet()) {
                    for (Field field : fields) {
                        if (field.getName().equals(key)) {
                            field.setAccessible(true);
                            Object fieldValue = deserializeData(jsonObj.get(key));
                            if (toWrapper(field.getType()).equals(Character.class)) {
                                field.set(obj, ((String) fieldValue).charAt(0));
                            } else if (Number.class.isAssignableFrom(toWrapper(field.getType())) && !Number.class.equals(field.getType())) {
                                Number n = (Number) fieldValue;
                                if(toWrapper(field.getType()).equals(Integer.class)){
                                    field.set(obj, n.intValue());
                                }else if(toWrapper(field.getType()).equals(Short.class)){
                                    field.set(obj, n.shortValue());
                                }else if(toWrapper(field.getType()).equals(Long.class)){
                                    field.set(obj, n.longValue());
                                }else if(toWrapper(field.getType()).equals(Byte.class)){
                                    field.set(obj, n.byteValue());
                                }else if(toWrapper(field.getType()).equals(Float.class)){
                                    field.set(obj, n.floatValue());
                                }else if(toWrapper(field.getType()).equals(Double.class)){
                                    field.set(obj, n.doubleValue());
                                }
                            } else {
                                field.set(obj, fieldValue);
                            }
                            break;
                        }
                    }
                }
            } while (!(c = c.getSuperclass()).equals(Object.class));
            return obj;
        } catch (NoSuchMethodException ex) {
            throw new SofofException("no default constructor avilable. Sofof can not instantiate an object");
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | InstantiationException ex) {
            throw new SofofException(ex);
        }
    }

    private static List<Class<?>> getBoxingTypes() {
        ArrayList<Class<?>> ret = new ArrayList<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        return ret;
    }

    private static boolean isBoxingType(Class c) {
        return BOXING_TYPES.contains(c);
    }

    private Class<?> toWrapper(Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return clazz;
        }

        if (clazz == Integer.TYPE) {
            return Integer.class;
        }
        if (clazz == Long.TYPE) {
            return Long.class;
        }
        if (clazz == Boolean.TYPE) {
            return Boolean.class;
        }
        if (clazz == Byte.TYPE) {
            return Byte.class;
        }
        if (clazz == Character.TYPE) {
            return Character.class;
        }
        if (clazz == Float.TYPE) {
            return Float.class;
        }
        if (clazz == Double.TYPE) {
            return Double.class;
        }
        if (clazz == Short.TYPE) {
            return Short.class;
        }
        if (clazz == Void.TYPE) {
            return Void.class;
        }

        return clazz;
    }

}
