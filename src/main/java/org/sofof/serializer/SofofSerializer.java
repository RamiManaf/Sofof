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
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private static final byte STRING = 12;
    private static final byte REFERECE = 13;
    private static List<ClassSpecificSerializer> customSerializers = new ArrayList<>();

    /**
     * return a list of custom serializers. you can add your custom serializer
     * to this list
     *
     * @return
     */
    public static List<ClassSpecificSerializer> getCustomSerializers() {
        return customSerializers;
    }

    static {
        customSerializers.add(new ClassSerializer());
        customSerializers.add(new CollectionSerializer());
        customSerializers.add(new FileSerializer());
    }

    @Override
    public String getName() {
        return "sofof";
    }

    @Override
    public void serialize(Object obj, OutputStream o) throws SofofException {
        Objects.nonNull(obj);
        DataOutputStream out = new DataOutputStream(o);
        try {
            out.write(2);
            //create a temporary byte array stream to know the size of the serialized object
            ByteArrayOutputStream temp = new ByteArrayOutputStream();
            writeData(obj, new DataOutputStream(temp), new ArrayList());
            out.writeInt(temp.size());
            out.write(temp.toByteArray());
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
    }

    private void writeData(Object obj, DataOutputStream out, List references) throws SofofException {
        try {
            Class c = (obj == null ? null : obj.getClass());
            if (obj == null) {
                out.write(NULL);
            } else if (c.isPrimitive() || isBoxingType(c)) {
                writePrimitive(obj, out);
            } else if (references.contains(obj)) {
                out.write(REFERECE);
                out.writeShort(references.indexOf(obj));
            } else if (obj instanceof String) {
                references.add(obj);
                out.write(STRING);
                out.writeShort(references.size() - 1);
                out.writeUTF((String) obj);
            } else if (c.isArray()) {
                references.add(obj);
                out.write(ARRAY);
                out.writeShort(references.size() - 1);
                out.writeUTF(c.getComponentType().getName());
                out.writeInt(Array.getLength(obj));
                for (int i = 0; i < Array.getLength(obj); i++) {
                    writeData(Array.get(obj, i), out, references);
                }
            } else {
                writeObject(obj, out, references);
            }
        } catch (IOException | IllegalAccessException ex) {
            throw new SofofException(ex);
        }
    }

    private void writeObject(Object obj, DataOutputStream out, List references) throws IOException, IllegalArgumentException, IllegalAccessException, SofofException {
        references.add(obj);
        out.write(OBJECT);
        out.writeShort(references.size() - 1);
        out.writeUTF(obj.getClass().getName());
        if (obj instanceof ClassSpecificSerializer) {
            ((ClassSpecificSerializer) obj).serialize(this, obj, out);
            return;
        }
        for (ClassSpecificSerializer serializer : customSerializers) {
            if (serializer.getClazz().isAssignableFrom(obj.getClass())) {
                serializer.serialize(this, obj, out);
                return;
            }
        }
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
    public Object deserialize(InputStream in) throws SofofException, ClassNotFoundException {
        try {
            if (in.read() == -1) {
                return null;
            }
            return deserializeData(new DataInputStream(new ByteArrayInputStream(readWholeByteArray(in, ByteBuffer.wrap(readWholeByteArray(in, 4)).getInt()))), new HashMap<>());
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
    }

    @Override
    public void skip(InputStream in) throws SofofException {
        try {
            if (in.read() == -1) {
                return;
            }
            in.skip(ByteBuffer.wrap(readWholeByteArray(in, 4)).getInt());
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
    }

    private byte[] readWholeByteArray(InputStream in, int size) throws SofofException, IOException {
        byte[] byteArray = new byte[size];
        int position = 0;
        while (position < byteArray.length) {
            int bytesRead = in.read(byteArray, position, byteArray.length - position);
            if (bytesRead == -1) {
                throw new SofofException(new EOFException("had read only " + position + " of " + byteArray.length + " and end of stream is reached"));
            }
            position += bytesRead;
        }
        return byteArray;
    }

    private Object deserializeData(DataInputStream in, Map<Short, Object> references) throws SofofException {
        try {
            byte type = in.readByte();
            if (type == NULL) {
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
                    throw new SofofException("data is malformed. make sure you are deserializing serialized bytes by sofof serializer");
                }
            } else if (type == REFERECE) {
                return references.get(in.readShort());
            } else if (type == STRING) {
                short referenceKey = in.readShort();
                String string = in.readUTF();
                references.put(referenceKey, string);
                return string;
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
            } else if (type == OBJECT) {
                return readObject(in, references);
            } else {
                throw new SofofException("data is malformed. make sure you are deserializing serialized bytes by sofof serializer");
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new SofofException(ex);
        }
    }

    private Object readObject(DataInputStream in, Map<Short, Object> references) throws IOException, SofofException {
        short referenceKey = in.readShort();
        String className = in.readUTF();
        try {
            Class c = Class.forName(className);
            if (ClassSpecificSerializer.class.isAssignableFrom(c)) {
                Constructor constructor = c.getDeclaredConstructor();
                constructor.setAccessible(true);
                ClassSpecificSerializer obj = (ClassSpecificSerializer) constructor.newInstance();
                Object result = obj.deserialize(this, c, in);
                references.put(referenceKey, result);
                return result;
            }
            for (ClassSpecificSerializer serializer : customSerializers) {
                if (serializer.getClazz().isAssignableFrom(c)) {
                    Object result = serializer.deserialize(this, c, in);
                    references.put(referenceKey, result);
                    return result;
                }
            }
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
            throw new SofofException("no default constructor avilable for class " + className);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | InstantiationException ex) {
            throw new SofofException(ex);
        }
    }

    private static class ClassSerializer implements ClassSpecificSerializer<Class> {

        @Override
        public Class<Class> getClazz() {
            return Class.class;
        }

        @Override
        public void serialize(Serializer serializer, Class obj, OutputStream o) throws SofofException {
            DataOutputStream out = new DataOutputStream(o);
            try {
                out.writeUTF(obj.getName());
            } catch (IOException ex) {
                throw new SofofException(ex);//imposible
            }
        }

        @Override
        public Class deserialize(Serializer serializer, Class clazz, InputStream in) throws SofofException {
            try {
                return Class.forName(new DataInputStream(in).readUTF());
            } catch (IOException | ClassNotFoundException ex) {
                throw new SofofException(ex);
            }
        }

    }

    private static class CollectionSerializer implements ClassSpecificSerializer<Collection> {

        @Override
        public Class<Collection> getClazz() {
            return Collection.class;
        }

        @Override
        public void serialize(Serializer serializer, Collection obj, OutputStream o) throws SofofException {
            DataOutputStream out = new DataOutputStream(o);
            try {
                out.writeInt(obj.size());
                for (Object element : obj) {
                    serializer.serialize(element, out);
                }
            } catch (IOException ex) {
                throw new SofofException(ex);
            }
        }

        @Override
        public Collection deserialize(Serializer serializer, Class clazz, InputStream in) throws SofofException {
            DataInputStream dis = new DataInputStream(in);
            try {
                Constructor constructor = clazz.getConstructor();
                Collection collection = (Collection) constructor.newInstance();
                int size = dis.readInt();
                for (int i = 0; i < size; i++) {
                    collection.add(serializer.deserialize(dis));
                }
                return collection;
            } catch (IOException
                    | ClassNotFoundException
                    | SecurityException
                    | InstantiationException
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException ex) {
                throw new SofofException(ex);
            } catch (NoSuchMethodException ex) {
                throw new SofofException("no default constructor avilable for class " + clazz.getName(), ex);
            }
        }

    }

    private static class FileSerializer implements ClassSpecificSerializer<File> {

        @Override
        public Class<File> getClazz() {
            return File.class;
        }

        @Override
        public void serialize(Serializer serializer, File obj, OutputStream out) throws SofofException {
            DataOutputStream dos = new DataOutputStream(out);
            try {
                dos.writeUTF(obj.getPath());
            } catch (IOException ex) {
                throw new SofofException(ex);
            }
        }

        @Override
        public File deserialize(Serializer serializers, Class clazz, InputStream in) throws SofofException {
            DataInputStream dis = new DataInputStream(in);
            try {
                return new File(dis.readUTF());
            } catch (IOException ex) {
                throw new SofofException(ex);
            }
        }

    }

}
