/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.sofof.SofofException;

/**
 * Sofof default serializer. This serializer stores the fields names and their
 * values. You can remove fields or create new one without any problem but if
 * you changed the field name it will be treated as a new field. You don't have
 * to implement Serializable interface in your classes but if you do this
 * serializer will serialize them using java serializing API. If the serialized
 * class doesn't implement Serializable interface then it must have a default
 * constructor with no arguments.
 *
 * @author Rami Manaf Abdullah
 */
public class SofofSerializer implements Serializer {

    private static final byte NULL = 0;
    private static final byte PRIMITIVE = 1;
    private static final byte ARRAY = 2;
    private static final byte OBJECT = 3;
    private static final byte INT = 4;
    private static final byte SHORT = 5;
    private static final byte BYTE = 6;
    private static final byte LONG = 7;
    private static final byte FLOAT = 8;
    private static final byte DOUBLE = 9;
    private static final byte BOOLEAN = 10;
    private static final byte CHAR = 11;
    private static final byte JAVA_SERIALIZER_OBJECT = 12;
    private static final byte REFERECE = 13;

    private JavaSerializer javaSerializer = new JavaSerializer();

    @Override
    public String getName() {
        return "sofof";
    }

    @Override
    public byte[] serialize(Object obj) throws SofofException {
        Objects.nonNull(obj);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteOut);
        try {
            out.write(1);
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
        writeData(obj, out, new ArrayList());
        return byteOut.toByteArray();
    }

    private void writeData(Object obj, DataOutputStream out, List references) throws SofofException {
        try {
            Class c = (obj == null ? null : obj.getClass());
            if (obj == null) {
                out.write(NULL);
            } else if (c.isPrimitive() || isBoxingType(c)) {
                writePrimitive(obj, out);
            } else if (c.isArray() && !c.getComponentType().isArray()) {
                if (references.contains(obj)) {
                    out.write(REFERECE);
                    out.writeShort(references.indexOf(obj));
                } else {
                    references.add(obj);
                    out.write(ARRAY);
                    out.writeShort(references.size() - 1);
                    out.writeUTF(c.getCanonicalName().substring(0, c.getCanonicalName().length() - 2));
                    out.writeInt(Array.getLength(obj));
                    for (int i = 0; i < Array.getLength(obj); i++) {
                        writeData(Array.get(obj, i), out, references);
                    }
                }
            } else {
                writeObject(obj, out, references);
            }
        } catch (IOException | IllegalAccessException ex) {
            throw new SofofException(ex);
        }
    }

    private void writeObject(Object obj, DataOutputStream out, List references) throws IOException, IllegalArgumentException, IllegalAccessException, SofofException {
        if (references.contains(obj)) {
            out.write(REFERECE);
            out.writeShort(references.indexOf(obj));
        } else {
            references.add(obj);
            if (obj instanceof Serializable || obj.getClass().isArray()) {
                out.write(JAVA_SERIALIZER_OBJECT);
                out.writeShort(references.size() - 1);
                byte[] result = javaSerializer.serialize(obj);
                out.writeInt(result.length);
                out.write(result);
            } else {
                out.write(OBJECT);
                out.writeShort(references.size() - 1);
                out.writeUTF(obj.getClass().getCanonicalName());
                Class c = obj.getClass();
                do {
                    List<Field> fields = getAllWritableFields(c);
                    out.writeShort(fields.size());
                    for (Field field : fields) {
                        out.writeUTF(field.getName());
                        field.setAccessible(true);
                        writeData(field.get(obj), out, references);
                    }
                } while (!(c = c.getSuperclass()).equals(Object.class));
            }
        }
    }

    private void writePrimitive(Object obj, DataOutputStream out) throws IllegalArgumentException, IllegalAccessException, IOException {
        out.write(PRIMITIVE);
        Class c = obj.getClass();
        if (c.equals(int.class) || c.equals(Integer.class)) {
            out.write(INT);
            out.writeInt((int) obj);
        } else if (c.equals(short.class) || c.equals(Short.class)) {
            out.write(SHORT);
            out.writeShort((short) obj);
        } else if (c.equals(byte.class) || c.equals(Byte.class)) {
            out.write(BYTE);
            out.writeByte((byte) obj);
        } else if (c.equals(long.class) || c.equals(Long.class)) {
            out.write(LONG);
            out.writeLong((long) obj);
        } else if (c.equals(float.class) || c.equals(Float.class)) {
            out.write(FLOAT);
            out.writeFloat((float) obj);
        } else if (c.equals(double.class) || c.equals(Double.class)) {
            out.write(DOUBLE);
            out.writeDouble((double) obj);
        } else if (c.equals(boolean.class) || c.equals(Boolean.class)) {
            out.write(BOOLEAN);
            out.writeBoolean((boolean) obj);
        } else if (c.equals(char.class) || c.equals(Character.class)) {
            out.write(CHAR);
            out.writeChar((char) obj);
        }
    }

    private static final List<Class<?>> BOXING_TYPES = getBoxingTypes();

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

    public static List<Field> getAllWritableFields(Class<?> type) {
        return Arrays.asList(type.getDeclaredFields()).stream().filter((f) -> !Modifier.isTransient(f.getModifiers())
                && !Modifier.isStatic(f.getModifiers())
                && !Modifier.isFinal(f.getModifiers())).collect(Collectors.toList());
    }

    @Override
    public Object deserialize(byte[] bytes) throws SofofException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        try {
            in.read();
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
        return deserializeData(in, new HashMap<>());
    }

    private Object deserializeData(DataInputStream in, Map<Short, Object> references) throws SofofException {
        try {
            byte type = in.readByte();
            if (type == REFERECE) {
                return references.get(in.readShort());
            } else if (type == NULL) {
                return null;
            } else if (type == PRIMITIVE) {
                byte t = in.readByte();
                if (t == INT) {
                    return in.readInt();
                } else if (t == SHORT) {
                    return in.readShort();
                } else if (t == BYTE) {
                    return in.readByte();
                } else if (t == LONG) {
                    return in.readLong();
                } else if (t == FLOAT) {
                    return in.readFloat();
                } else if (t == DOUBLE) {
                    return in.readDouble();
                } else if (t == BOOLEAN) {
                    return in.readBoolean();
                } else if (t == CHAR) {
                    return in.readChar();
                } else {
                    throw new SofofException("data is malformed. make sure you are deserializing serialized bytes by this serializer");
                }
            } else if (type == ARRAY) {
                short referenceKey = in.readShort();
                String className = in.readUTF();
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
                int length = in.readInt();
                Object array = Array.newInstance(c, length);
                for (int i = 0; i < length; i++) {
                    Array.set(array, i, deserializeData(in, references));
                }
                references.put(referenceKey, array);
                return array;
            } else if (type == JAVA_SERIALIZER_OBJECT) {
                short referenceKey = in.readShort();
                byte[] bytes = new byte[in.readInt()];
                in.readFully(bytes);
                Object result = javaSerializer.deserialize(bytes);
                references.put(referenceKey, result);
                return result;
            } else if (type == OBJECT) {
                return readObject(in, references);
            } else {
                throw new SofofException("data is malformed. make sure you are deserializing serialized bytes by this serializer");
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new SofofException(ex);
        }
    }

    private Object readObject(DataInputStream in, Map<Short, Object> references) throws IOException, SofofException {
        try {
            short referenceKey = in.readShort();
            Class c = Class.forName(in.readUTF());
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object obj = constructor.newInstance();
            do {
                List<Field> fields = getAllWritableFields(c);
                short fieldsCount = in.readShort();
                for (short i = 0; i < fieldsCount; i++) {
                    String fieldName = in.readUTF();
                    for (Field field : fields) {
                        if (field.getName().equals(fieldName)) {
                            field.setAccessible(true);
                            field.set(obj, deserializeData(in, references));
                            break;
                        }
                    }
                }
            } while (!(c = c.getSuperclass()).equals(Object.class));
            references.put(referenceKey, obj);
            return obj;
        } catch (NoSuchMethodException ex) {
            throw new SofofException("no default constructor avilable. Sofof can not instantiate an object");
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | InstantiationException ex) {
            throw new SofofException(ex);
        }
    }

}
