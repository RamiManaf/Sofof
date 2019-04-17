# قاعدة بيانات صفوف
صفوف هي قاعدة بيانات سهلة, متنقلة ومتعددة المستخدمين لمبرمجي جافا SE و EE
## Example
```java
import org.sofof.command.Bind;
import org.sofof.permission.User;
import java.io.File;

public class Binding {
    public static void main(String[] args) throws SofofException {
        Database.createDatabase(new File("sofof"));
        Server s = new Server(new File("sofof"), 6969, false);
        s.getUsers().add(new User("rami", "secret"));
        s.startUp();
        try (Session sess = new Database("localhost", 6969).startSession(new User("rami", "secret"), false)) {
            sess.execute(new Bind("قاعدة بيانات صفوف رائعة").to("المنشورات"));
        }
    }
}
```

## نقطة البداية
لتستطيع البدء باستخدام قاعدة بيانات صفوف يمكنك تنزيل آخر إصدار لقاعدة البيانات من  [هنا](https://github.com/RamiManaf/Sofof/releases/) وقراءة [الدليل](https://github.com/RamiManaf/Sofof/wiki/Getting Started)
