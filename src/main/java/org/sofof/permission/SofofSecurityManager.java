/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.permission;

import org.sofof.SofofException;
import org.sofof.annotation.Command;
import org.sofof.command.Executable;
import org.sofof.command.Query;

/**
 *<p>تستخدم هذه النافذة مع مدير الحماية, وفي حال تم تحديد مدير حماية يستخدم هذه النافذة سيتم استقبال جميع طلبات التحقق من الصلاحيات عبر الدوال المعرفة في هذه النافذة.</p>
 * @author Rami Manaf Abdullah
 */
public interface SofofSecurityManager {
    
    /**
     * تقوم هذه الدالة بالتأكد من أن من أصدر أمر القيام بعملية ما هو أحد أوامر صفوف, ويجب استخدام هذه الدالة في حال أريد منع أو السماح لأوامر صفوف بالقيام ببعض العمليات كالتعديل على الملفات وغيرها,
     * ويتم الحصول على مصفوفة الصفوف من الدالة التالية في مدير الحماية
     * getClassContext()
     * @param classes الصفوف لتي تعيدها الدالة getClassContext من كائن مدير الحماية
     * @return صحيح إن كان المسؤول عن العملية هو أحد أوامر صفوف وخاطئ في غير ذلك
     */
    public static boolean isFromCommand(Class[] classes){
        for(Class clazz : classes){
            if(clazz.isAnnotationPresent(Command.class))return true;
        }
        return false;
    }
    
    /**
     * تنادى هذه الدالة في حالة طلب أحد مستخدمي قاعدة البيانات إذنا للاستعلام
     * @param user المستخدم الذي طلب الإذن بتنفيذ الاستعلام
     * @param query الاستعلام
     * @throws SecurityException يتم إطلاقها في حال عدم إعطاء المستخدم الإذن
     */
    public void checkQuery(User user, Query query) throws SecurityException;
        
    /**
     * تنادى هذه الدالة في حالة طلب أحد مستخدمي قاعدة البيانات إذنا لتنفيذ أمر تنفيذي
     * @param user المستخدم الذي طلب الإذن لتنفيذ أمر تنفيذي
     * @param executable الأمر التنفيذي
     * @throws SecurityException يتم إطلاقها في حال عدم إعطاء المستخدم الإذن
     */
    public void checkExecutable(User user, Executable executable) throws SecurityException;
    
}
