/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.SofofException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

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
 * <td>يجب إحاطتها بإشارة اقتباس غير مزدوجة</td>
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

    public static Object execute(String expression, Object obj) throws SofofException {
        if (!expression.startsWith("#")) {
            throw new SofofException("not a valide expression. # misseds");
        }
        expression = expression.substring(1);
        if (expression.isEmpty()) {
            return obj;
        }
        if (expression.equalsIgnoreCase("true") || expression.equalsIgnoreCase("false")) {
            return Boolean.valueOf(expression);
        } else if (expression.matches("[\\d]*[.]?[\\d]+")) {
            return Double.valueOf(expression);
        }
        String[] fomArr = expression.split("[.]");
        List<String> tokens = new ArrayList<>();
        for (int x = 0; x < fomArr.length; x++) {
            if (!fomArr[x].matches("([a-zA-Z0-9_$]+)*\\(.*\\)")
                    && !fomArr[x].matches("([a-zA-Z0-9_$]+) *")) {
                fomArr[x + 1] = fomArr[x] + fomArr[x + 1];
            } else {
                tokens.add(fomArr[x]);
            }
        }
        for (String fom : tokens) {
            if (fom.contains("(")) {
                fom = fom.subSequence(0, fom.length() - 1).toString();
                String[] np = fom.split("[(]");
                Vector<Object> params = null;
                Class[] classes = null;
                if (np.length == 2) {
                    params = new Vector<>(Arrays.asList(np[1].split("[,]")));
                    classes = new Class[params.size()];
                    for (int x = 0; x < params.size(); x++) {
                        String param = ((String) params.get(x)).trim();
                        if (param.startsWith("'") && (param.endsWith("C") || param.endsWith("c"))) {
                            classes[x] = param.endsWith("O") ? Object.class : param.endsWith("C") ? Character.class : char.class;
                            params.set(x, param.trim().charAt(1));
                        } else if (param.startsWith("'")) {
                            classes[x] = param.endsWith("O") ? Object.class : String.class;
                            params.set(x, param.trim().subSequence(1, param.length() - 2).toString());
                        } else if (param.startsWith("true") || param.startsWith("false")) {
                            classes[x] = param.endsWith("O") ? Object.class : param.endsWith("B") ? Boolean.class : boolean.class;
                            params.set(x, Boolean.valueOf(param.substring(0, param.length() - 1)));
                        } else {
                            if (param.substring(0, param.length() - 1).matches("[\\d]*[.]{1}[\\d]+")) {
                                switch (param.charAt(param.length() - 1)) {
                                    case 'D': {
                                        classes[x] = Double.class;
                                        params.set(x, Double.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'd': {
                                        classes[x] = double.class;
                                        params.set(x, Double.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'F': {
                                        classes[x] = Float.class;
                                        params.set(x, Float.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'f': {
                                        classes[x] = float.class;
                                        params.set(x, Float.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'O': {
                                        classes[x] = Object.class;
                                        params.set(x, Float.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    default: {
                                        classes[x] = float.class;
                                        params.set(x, Float.valueOf(param.substring(0, param.length() - 1)));
                                    }
                                }
                            } else {
                                switch (param.charAt(param.length() - 1)) {
                                    case 'I': {
                                        classes[x] = Integer.class;
                                        params.set(x, Integer.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'i': {
                                        classes[x] = int.class;
                                        params.set(x, Integer.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'l': {
                                        classes[x] = long.class;
                                        params.set(x, Long.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'L': {
                                        classes[x] = Long.class;
                                        params.set(x, Long.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'H': {
                                        classes[x] = Short.class;
                                        params.set(x, Short.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'h': {
                                        classes[x] = short.class;
                                        params.set(x, Short.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'b': {
                                        classes[x] = byte.class;
                                        params.set(x, Byte.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'B': {
                                        classes[x] = Byte.class;
                                        params.set(x, Byte.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }

                                    case 'O': {
                                        classes[x] = Object.class;
                                        params.set(x, Integer.valueOf(param.substring(0, param.length() - 1)));
                                        break;
                                    }
                                    default: {
                                        classes[x] = int.class;
                                        params.set(x, Integer.valueOf(param.substring(0, param.length() - 1)));
                                    }
                                }
                            }
                        }
                    }
                }
                try {
                    Method method = obj.getClass().getMethod(np[0], classes != null ? (Class<?>[]) classes : new Class[0]);
                    method.setAccessible(true);
                    obj = method.invoke(obj, params != null ? params.toArray() : new Object[0]);
                } catch (NoSuchMethodException ex) {
                    throw new SofofException("there is no method with name " + np[0] + " and params " + Arrays.toString(classes));
                } catch (SecurityException ex) {
                } catch (IllegalAccessException ex) {
                    throw new SofofException("the method name " + np[0] + " with params " + classes.toString() + " is private or protected");
                } catch (IllegalArgumentException ex) {
                } catch (InvocationTargetException ex) {
                    throw new SofofException("the method name " + np[0] + " with params " + classes.toString() + " threw " + ex.getClass().getName() + "\n"
                            + ex.getTargetException().getMessage());
                }
            } else {
                try {
                    Field field = obj.getClass().getField(fom);
                    field.setAccessible(true);
                    obj = field.get(obj);
                } catch (NoSuchFieldException ex) {
                    throw new SofofException("no such field with name " + fom);
                } catch (SecurityException ex) {
                    throw new SofofException(ex.getMessage());
                } catch (IllegalArgumentException ex) {
                } catch (IllegalAccessException ex) {
                    throw new SofofException("the field " + fom + " is private or protected");
                }
            }
        }
        return obj;
    }

}
