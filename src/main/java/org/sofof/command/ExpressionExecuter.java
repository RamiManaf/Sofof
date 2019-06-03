/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import java.lang.reflect.Array;
import org.sofof.SofofException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * <h3>النص التنفيذي</h3>
 * يسمح النص التنفيذي بالقيام بعمليات على الكائن الممرر, ويتم ذلك من خلال تنفيذ
 * دوال الكائن وإعادة ناتج تنفيذها أو استدعاء الحقول. يجب أن يبدأ النص التنفيذي
 * بعلامة #, ويتم بعدها كتابة اسم الدالة أو الحقل, ويمكن تنفيذ نفس العمليات أيضا
 * على الحقل المستدعى أو على ما تعيد الدلة المنفذة, وذلك يتم بكتابة نقطة كما في
 * لغة جافا. وهذا مثال: #getAdmin().name.toUpperCase() ويمكن أيضا استخدام بيانات
 * جافا البدائية والنصوص لتمريرها كمعاملات للدوال
 * <table border="1" summary="التعبير عن البيانات">
 * <tr>
 * <th>نوع البيانات</th>
 * <th>الرمز المعبر عنه</th>
 * </tr>
 * <tr>
 * <td>String</td>
 * <td>S</td>
 * <td>يجب إحاطتها بإشارة اقتباس مزدوجة</td>
 * </tr>
 * <tr>
 * <td>Character</td>
 * <td>C</td>
 * <td>يجب إحاطتها بإشارة اقتباس غير مزدوجة</td>
 * </tr>
 * <tr>
 * <td>char</td>
 * <td>c</td>
 * <td>يجب إحاطتها بإشارة اقتباس غير مزدوجة</td>
 * </tr>
 * <tr>
 * <td>int</td>
 * <td>i</td>
 * </tr>
 * <tr>
 * <td>Integer</td>
 * <td>I</td>
 * </tr>
 * <tr>
 * <td>long</td>
 * <td>l</td>
 * </tr>
 * <tr>
 * <td>Long</td>
 * <td>L</td>
 * </tr>
 * <tr>
 * <td>short</td>
 * <td>h</td>
 * </tr>
 * <tr>
 * <td>Short</td>
 * <td>H</td>
 * </tr>
 * <tr>
 * <td>double</td>
 * <td>d</td>
 * </tr>
 * <tr>
 * <td>Double</td>
 * <td>D</td>
 * </tr>
 * <tr>
 * <td>float</td>
 * <td>f</td>
 * </tr>
 * <tr>
 * <td>Float</td>
 * <td>F</td>
 * </tr>
 * <tr>
 * <td>byte</td>
 * <td>b</td>
 * </tr>
 * <tr>
 * <td>Byte</td>
 * <td>B</td>
 * </tr>
 * <tr>
 * <td>Object</td>
 * <td>O</td>
 * <td>يمكن استخدامه فقط مع String,Character,Float,Integer</td>
 * </tr>
 * </table>
 * وهذا مثال: #getPlayer('String to Object arg'O, 'char'c, 2.4f)
 *
 *
 * @author Rami Manaf Abdullah
 */
public class ExpressionExecuter {

    private static final Pattern DIGIT = Pattern.compile("[\\d]*[.]?[\\d]+");
    private static final Pattern DECIMAL_POINT = Pattern.compile("[\\d]*[.]{1}[\\d]+");
    private static final Pattern METHOD = Pattern.compile("([a-zA-Z0-9_$]+)*\\(.*\\)(\\[\\d\\])?");
    private static final Pattern FIELD = Pattern.compile("([a-zA-Z0-9_$]+)*(\\[\\d\\])?");

    public static Object execute(String expression, Object obj) throws SofofException {
        //expression starter
        if (!expression.startsWith("#")) {
            throw new SofofException("not a valide expression. # misseds");
        }
        expression = expression.substring(1);
        if (expression.isEmpty()) {
            return obj;
        }
        //parse to a data type
        if (expression.startsWith("'") || expression.startsWith("\"") || Character.isDigit(expression.charAt(0)) || expression.equals("true") || expression.equals("false")) {
            return parseDataType(expression)[1];
        }
        //expression in this point is either a field or method
        List<String> fieldsAndMethodes = fieldsAndMethodesAssempler(expression.split("[.]"));
        for (String fieldOrMethod : fieldsAndMethodes) {
            int arrayIndex = -1;
            //check if the method or parameter ends with an array brackets
            if (fieldOrMethod.endsWith("]")) {
                String[] temp = fieldOrMethod.substring(0, fieldOrMethod.length() - 1).split("\\[");
                arrayIndex = Integer.parseInt(temp[temp.length - 1]);
                fieldOrMethod = "";
                for (int x = 0; x < temp.length - 1; x++) {
                    fieldOrMethod = fieldOrMethod.concat(temp[x]);
                }
            }
            //check if the fieldOrMethod variable is a field or method
            if (fieldOrMethod.contains("(")) {
                fieldOrMethod = fieldOrMethod.substring(0, fieldOrMethod.length() - 1);
                String[] nameAndParameters = fieldOrMethod.split("[(]");
                String name = nameAndParameters[0];
                String parameters = "";
                for (int i = 1; i < nameAndParameters.length; i++) {
                    parameters = parameters.concat(nameAndParameters[i]);
                }
                Vector<Object> params = new Vector<>();
                Class[] classes = new Class[0];
                if (!parameters.isEmpty()) {
                    params.addAll(methodParametersAssempler(parameters.split("[,]")));
                    classes = new Class[params.size()];
                    for (int x = 0; x < params.size(); x++) {
                        Object[] dataTypeAndValue = parseDataType(((String) params.get(x)).trim());
                        classes[x] = (Class) dataTypeAndValue[0];
                        params.set(x, dataTypeAndValue[1]);
                    }
                }
                try {
                    Method method = obj.getClass().getMethod(name, classes);
                    method.setAccessible(true);
                    obj = method.invoke(obj, params.toArray());
                } catch (NoSuchMethodException ex) {
                    throw new SofofException("there is no method with name " + name + " and params " + Arrays.toString(classes));
                } catch (SecurityException ex) {
                } catch (IllegalAccessException ex) {
                    throw new SofofException("the method name " + name + " with params " + classes.toString() + " is private or protected");
                } catch (IllegalArgumentException ex) {
                } catch (InvocationTargetException ex) {
                    throw new SofofException("the method name " + name + " with params " + classes.toString() + " threw " + ex.getClass().getName() + "\n"
                            + ex.getTargetException().getMessage());
                }
            } else {
                try {
                    Field field = obj.getClass().getField(fieldOrMethod);
                    field.setAccessible(true);
                    obj = field.get(obj);
                } catch (NoSuchFieldException ex) {
                    throw new SofofException("no such field with name " + fieldOrMethod);
                } catch (SecurityException ex) {
                    throw new SofofException(ex.getMessage());
                } catch (IllegalArgumentException ex) {
                } catch (IllegalAccessException ex) {
                    throw new SofofException("the field " + fieldOrMethod + " is private or protected");
                }
            }
            if (arrayIndex != -1) {
                obj = Array.get(obj, arrayIndex);
            }
        }
        return obj;
    }

    private static ArrayList<String> methodParametersAssempler(String[] parameters) throws SofofException {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            String parameter = parameters[i].trim();
            if (parameter.startsWith("'") && !parameter.substring(0, parameter.length() - 1).endsWith("'")) {
                if (i + 1 < parameters.length) {
                    parameters[i + 1] = parameters[i] + parameters[i + 1];
                } else {
                    throw new SofofException(parameters[i] + "don't match the expected regex");
                }
            } else {
                result.add(parameter);
            }
        }
        return result;
    }

    private static ArrayList<String> fieldsAndMethodesAssempler(String[] chunkes) throws SofofException {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < chunkes.length; i++) {
            if (!FIELD.matcher(chunkes[i]).matches() && !METHOD.matcher(chunkes[i]).matches()) {
                if (i + 1 < chunkes.length) {
                    chunkes[i + 1] = chunkes[i] + chunkes[i + 1];
                } else {
                    throw new SofofException("The expresion " + chunkes[i] + " is not a method nor a field name");
                }
            } else {
                result.add(chunkes[i]);
            }
        }
        return result;
    }

    private static Object[] parseDataType(String data) {
        Class type;
        Object value;
        if (data.startsWith("'")) {
            type = data.endsWith("O") ? Object.class : data.endsWith("C") ? Character.class : char.class;
            value = data.trim().charAt(1);
        } else if (data.startsWith("\"")) {
            type = data.endsWith("O") ? Object.class : String.class;
            value = data.trim().substring(1, data.endsWith("\"") ? data.length() - 1 : data.length() - 2);
        } else if (data.startsWith("true") || data.startsWith("false")) {
            type = data.endsWith("O") ? Object.class : data.endsWith("B") ? Boolean.class : boolean.class;
            value = Boolean.valueOf(data.length() == 4 ? data : data.substring(0, data.length() - 1));
        } else {
            if (DECIMAL_POINT.matcher(data.substring(0, data.length() - 1)).matches() || DECIMAL_POINT.matcher(data).matches()) {
                switch (data.charAt(data.length() - 1)) {
                    case 'D': {
                        type = Double.class;
                        value = Double.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'd': {
                        type = double.class;
                        value = Double.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'F': {
                        type = Float.class;
                        value = Float.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'f': {
                        type = float.class;
                        value = Float.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'O': {
                        type = Object.class;
                        value = Float.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    default: {
                        type = float.class;
                        value = Float.valueOf(Character.isDigit(data.charAt(data.length() - 1)) ? data : data.substring(0, data.length() - 1));
                    }
                }
            } else {
                switch (data.charAt(data.length() - 1)) {
                    case 'I': {
                        type = Integer.class;
                        value = Integer.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'i': {
                        type = int.class;
                        value = Integer.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'l': {
                        type = long.class;
                        value = Long.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'L': {
                        type = Long.class;
                        value = Long.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'H': {
                        type = Short.class;
                        value = Short.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'h': {
                        type = short.class;
                        value = Short.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'b': {
                        type = byte.class;
                        value = Byte.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'B': {
                        type = Byte.class;
                        value = Byte.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }

                    case 'O': {
                        type = Object.class;
                        value = Integer.valueOf(data.substring(0, data.length() - 1));
                        break;
                    }
                    default: {
                        type = int.class;
                        value = Integer.valueOf(Character.isDigit(data.charAt(data.length() - 1)) ? data : data.substring(0, data.length() - 1));
                    }
                }
            }
        }
        return new Object[]{type, value};
    }

}
