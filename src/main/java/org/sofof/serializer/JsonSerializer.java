/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
 * first element in arrays or collections so it can deserialize them later. This
 * serializer doesn't support circular references
 *
 * @author Rami Manaf Abdullah
 */
public class JsonSerializer implements Serializer {

    private static final List<Class<?>> BOXING_TYPES = getBoxingTypes();
    private static final List<ClassSpecificSerializer> customSerializers = new ArrayList<>();
    private final byte BRACKET = 0b01111011;//{
    private final byte COMMA = 0b00101100;//,
    private final byte CLOSED_BRACKET = 0b01111101;//}
    private final byte SQUARE_BRACKET = 0b01011011;//[
    private final byte CLOSED_SQUARE_BRACKET = 0b01011101;//]
    private final byte QUOTATION = 0b00100111;//'
    private final byte DOUBLE_QUOTATION = 0b00100010;//"
    private final byte BACK_SLASH = 0b01011100;//\

    static {
        customSerializers.add(new ClassSerializer());
    }

    private static boolean pretty = false;

    /**
     * return a list of custom serializers. you can add your custom serializer
     * to this list
     *
     * @return
     */
    public static List<ClassSpecificSerializer> getCustomSerializers() {
        return customSerializers;
    }

    /**
     * @return true if the serializer is set to print pretty json
     */
    public static boolean isPretty() {
        return pretty;
    }

    /**
     * sets the json printing in the database to be pretty. this is suggested
     * only for debuging. This option will increase characters that should be
     * printed in the database files.
     *
     * @param pretty
     */
    public static void setPretty(boolean pretty) {
        JsonSerializer.pretty = pretty;
    }

    @Override
    public String getName() {
        return "json";
    }

    @Override
    public byte[] getStartCode() {
        return new byte[]{BRACKET};
    }

    @Override
    public byte[] getSeparatorCode() {
        return new byte[]{COMMA};
    }

    @Override
    public byte[] getEndCode() {
        return new byte[]{CLOSED_BRACKET};
    }

    @Override
    public void serialize(Object obj, OutputStream out) throws SofofException {
        Objects.nonNull(obj);
        String json;
        if (obj.getClass().equals(String.class)) {
            json = "\"" + obj + "\"";
        } else {
            Object result = serializeData(obj);
            if (result instanceof JSONObject) {
                json = ((JSONObject) result).toString(2);
            } else if (result instanceof JSONArray) {
                json = ((JSONArray) result).toString(2);
            } else {
                json = result.toString();
            }
        }
        if (!json.startsWith("{") && !json.startsWith("[")) {
            json = "{\"value\":" + json + "}";
        }
        try {
            out.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
    }

    private Object serializeData(Object obj) throws SofofException {
        try {
            if (obj == null) {
                return null;
            }
            Class c = obj.getClass();
            if (c.equals(char.class) || c.equals(Character.class)) {
                return String.valueOf(obj);
            } else if (c.isPrimitive() || isBoxingType(c) || c.equals(String.class)) {
                return obj;
            } else if (c.isArray()) {
                JSONArray array = new JSONArray();
                array.put(c.getComponentType().getName());
                for (int i = 0; i < Array.getLength(obj); i++) {
                    array.put(serializeData(Array.get(obj, i)));
                }
                return array;
            } else if (Collection.class.isAssignableFrom(c)) {
                JSONArray array = new JSONArray();
                array.put(c.getName());
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
        if (obj instanceof ClassSpecificSerializer) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ((ClassSpecificSerializer) obj).serialize(this, obj, baos);
            return new JSONObject(new String(baos.toByteArray(), StandardCharsets.UTF_8));
        }
        Class c = obj.getClass();
        for (ClassSpecificSerializer serializer : customSerializers) {
            if (serializer.getClazz().equals(c)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                serializer.serialize(this, obj, baos);
                return new JSONObject(new String(baos.toByteArray(), StandardCharsets.UTF_8));
            }
        }
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("class", c.getName());
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

    private Object deserialize(byte[] bytes) throws SofofException {
        Object result = JSONObject.stringToValue(new String(bytes, StandardCharsets.UTF_8));
        if (result instanceof String) {
            String stringResult = (String) result;
            if (stringResult.startsWith("{")) {
                JSONObject obj = new JSONObject(stringResult);
                if (obj.has("value")) {
                    return obj.get("value");
                } else {
                    return deserializeData(obj);
                }
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
                //first element in the array contains the array class name
                String className = jsonArray.getString(0);
                //there is no direct way to get a primitive class
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
                    //maybe a non primitive data type array or a collection
                    c = Class.forName(className);
                }
                int length = jsonArray.length() - 1;
                Object array;
                if (Collection.class.isAssignableFrom(c)) {
                    Constructor constructor = c.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    array = constructor.newInstance();
                    for (int i = 0; i < length; i++) {
                        ((Collection) array).add(deserializeData(jsonArray.get(i + 1)));
                    }
                } else {
                    array = Array.newInstance(c, length);
                    for (int i = 0; i < length; i++) {
                        Object value = deserializeData(jsonArray.get(i + 1));
                        if (Number.class.isAssignableFrom(toWrapper(c)) && isBoxingType(value.getClass())) {
                            Array.set(array, i, cast(c, (Number) value));
                        } else {
                            Array.set(array, i, value);
                        }
                    }
                }
                return array;
            } else if (obj instanceof JSONObject) {
                return readObject((JSONObject) obj);
            } else {
                return obj;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new SofofException(ex);
        } catch (NoSuchMethodException ex) {
            throw new SofofException("no default constructor avilable. Sofof can not instantiate an object", ex);
        }
    }

    private Object cast(Class type, Number number) {
        type = toWrapper(type);
        if (type == Integer.class) {
            return number.intValue();
        }
        if (type == Long.class) {
            return number.longValue();
        }
        if (type == Byte.class) {
            return number.byteValue();
        }
        if (type == Float.class) {
            return number.floatValue();
        }
        if (type == Double.class) {
            return number.doubleValue();
        }
        if (type == Short.class) {
            return number.shortValue();
        }
        return number;
    }

    private Object readObject(JSONObject jsonObj) throws SofofException {
        try {
            Class c = Class.forName(jsonObj.getString("class"));
            if (ClassSpecificSerializer.class.isAssignableFrom(c)) {
                Constructor constructor = c.getDeclaredConstructor();
                constructor.setAccessible(true);
                ClassSpecificSerializer obj = (ClassSpecificSerializer) constructor.newInstance();
                return obj.deserialize(this, c, new ByteArrayInputStream(jsonObj.toString().getBytes(StandardCharsets.UTF_8)));
            }
            for (ClassSpecificSerializer serializer : customSerializers) {
                if (serializer.getClazz().equals(c)) {
                    return serializer.deserialize(this, c, new ByteArrayInputStream(jsonObj.toString().getBytes(StandardCharsets.UTF_8)));
                }
            }
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object obj = constructor.newInstance();
            do {
                List<Field> fields = getAllWritableFields(c);
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (!field.getType().isPrimitive()) {
                        field.set(obj, null);
                    }
                    for (String key : jsonObj.keySet()) {
                        if (field.getName().equals(key)) {
                            field.setAccessible(true);
                            Object fieldValue = deserializeData(jsonObj.get(key));
                            if (toWrapper(field.getType()).equals(Character.class)) {
                                field.set(obj, ((String) fieldValue).charAt(0));
                            } else if (Number.class.isAssignableFrom(toWrapper(field.getType())) && !Number.class.equals(field.getType())) {
                                Number n = (Number) fieldValue;
                                if (toWrapper(field.getType()).equals(Integer.class)) {
                                    field.set(obj, n.intValue());
                                } else if (toWrapper(field.getType()).equals(Short.class)) {
                                    field.set(obj, n.shortValue());
                                } else if (toWrapper(field.getType()).equals(Long.class)) {
                                    field.set(obj, n.longValue());
                                } else if (toWrapper(field.getType()).equals(Byte.class)) {
                                    field.set(obj, n.byteValue());
                                } else if (toWrapper(field.getType()).equals(Float.class)) {
                                    field.set(obj, n.floatValue());
                                } else if (toWrapper(field.getType()).equals(Double.class)) {
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
            throw new SofofException("no default constructor avilable. Sofof can not instantiate an object from class: " + jsonObj.getString("class"));
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

    @Override
    public Object deserialize(InputStream in) throws SofofException, ClassNotFoundException {
        try {
            int firstChar = in.read();

            if (firstChar == -1) {
                return null;
            }
            if (firstChar != BRACKET && firstChar != SQUARE_BRACKET) {
                throw new SofofException("trying to deserialize a non-json input stream");
            }
            int openBrackets = 1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(firstChar);
            byte lastByte = 0;
            boolean stringContext = false;
            while (openBrackets > 0) {
                int newByte = in.read();
                if ((newByte == BRACKET || newByte == SQUARE_BRACKET) && !stringContext) {
                    openBrackets++;
                }
                if ((newByte == CLOSED_BRACKET || newByte == CLOSED_SQUARE_BRACKET) && !stringContext) {
                    openBrackets--;
                }
                if ((newByte == DOUBLE_QUOTATION || newByte == QUOTATION) && lastByte != BACK_SLASH) {
                    stringContext = !stringContext;
                }
                baos.write(newByte);
                lastByte = (byte) newByte;
            }
            return deserialize(baos.toByteArray());
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
    }

    private static class CountingInputStreamFilter extends FilterInputStream {

        int bytesRead = 0;

        public CountingInputStreamFilter(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            bytesRead += b == -1 ? 0 : 1;
            return b;
        }

        @Override
        public int read(byte[] arr) throws IOException {
            int b = super.read(arr);
            bytesRead += b == -1 ? 0 : 1;
            return b;
        }

        @Override
        public int read(byte[] arr, int off, int len) throws IOException {
            int b = super.read(arr, off, len);
            bytesRead += b == -1 ? 0 : 1;
            return b;
        }

        public int getBytesRead() {
            return bytesRead;
        }
    }

    private static class ClassSerializer implements ClassSpecificSerializer<Class> {

        @Override
        public Class getClazz() {
            return Class.class;
        }

        @Override
        public void serialize(Serializer serializer, Class obj, OutputStream out) throws SofofException {
            try {
                out.write(("{\"class\":\"java.lang.Class\",\"name\":\"" + obj.getName() + "\"}").getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                throw new SofofException(ex);
            }
        }

        @Override
        public Class deserialize(Serializer serializer, Class clazz, InputStream in) throws SofofException {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                int i;
                while ((i = in.read()) != -1) {
                    bytes.write(i);
                }
                return Class.forName(new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8)).getString("name"));
            } catch (ClassNotFoundException | IOException ex) {
                throw new SofofException(ex);
            }
        }

    }
}
