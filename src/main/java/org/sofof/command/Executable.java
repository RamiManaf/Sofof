/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.SofofException;
import java.io.Serializable;
import org.sofof.ListInputStream;
import org.sofof.ListOutputStream;

/**
 *أمر يقوم  بالتعديل على قاعدة البيانات
 *@author Rami Manaf Abdullah
 */
public interface Executable extends Command, Serializable{
    
    /**
     * يتم تنفيذ الأمر في هذه الدالة بعد تمرير الكائنات
     * اللازمة للقراءة والكتابة من وإلى قاعدة البيانات
     * ولا يتم مناداة هذه الدالة مباشرة بل يتم مناداتها
     * من كائن الجلسة
     * @param in قارئ القوائم
     * @param out كاتب القوائم
     * @return عدد الكائنات المتأثرة من الأمر
     * @throws SofofException 
     */
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException;
    
}
