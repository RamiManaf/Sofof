# Sofof Database
Sofof is an easy, portable, multiusers database for Java Se and EE developers
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
            sess.execute(new Bind("Sofof database is great").to("posts"));
        }
    }
}
```

## Where to start?
to start use sofof database you can download it from  [here](https://github.com/RamiManaf/Sofof/releases/) and read the [documentation](https://github.com/RamiManaf/Sofof/wiki/Getting_Started_en)
## Other languages
[العربية](https://github.com/RamiManaf/Sofof/wiki/Home_ar)
