# Sofof Database
[Sofof](https://ramimanaf.github.io/Sofof/) is an easy, portable, multiusers , Object oriented database for Java SE and EE developers
## Example
```java
import org.sofof.command.Bind;
import org.sofof.permission.User;
import org.sofof.Server;
import org.sofof.SessionManager;
import java.io.File;

public class Binding {
    public static void main(String[] args) throws SofofException {
        Server s = new Server(new File("sofof"), 6969, false);
        s.createDatabase();
        s.getUsers().add(new User("rami", "secret"));
        s.startUp();
        try (Session sess = SessionManager.startSession("java:localhost:6969", new User("rami", "secret"), false)) {
            sess.execute(new Bind("Sofof database is great").to("posts"));
        }
    }
}
```

## Where to start?
to start use sofof database you can download it from  [here](https://github.com/RamiManaf/Sofof/releases/) and read the [documentation](https://github.com/RamiManaf/Sofof/wiki/Getting_Started_en)
```xml
<dependency>
  <groupId>io.github.ramimanaf</groupId>
  <artifactId>sofof</artifactId>
  <version>3.0.0</version>
</dependency>
```
## Other languages
[العربية](https://github.com/RamiManaf/Sofof/wiki/Home_ar)
