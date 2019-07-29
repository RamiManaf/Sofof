package org.sofof;


import org.sofof.Session;
import org.sofof.Server;
import org.sofof.SofofException;
import org.sofof.bean.Student;
import org.sofof.command.Bind;
import org.sofof.command.Select;
import org.sofof.command.Unbind;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author LENOVO PC
 */
public class StudentsTest extends Application {
    
    private TableView<Student> table;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Server server = new Server().configure().startUp();
        SessionManager.configure();
        Session session = SessionManager.getSession("test");
        table = new TableView(FXCollections.observableList(session.query(new Select(Student.class).from("students"))));
        table.setPrefSize(400, 600);
        table.setEditable(true);
        table.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Student, String> nameCol = new TableColumn<>("الاسم");
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Student, Integer> ageCol = new TableColumn<>("العمر");
        ageCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        TableColumn<Student, String> nationalityCol = new TableColumn<>("الجنسية");
        nationalityCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nationalityCol.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        table.getColumns().addAll(nameCol, ageCol, nationalityCol);
        table.setPlaceholder(new Text("لا يوجد بيانات طلاب لعرضها"));
        Button add = new Button("إضافة");
        add.setDefaultButton(true);
        add.setOnAction((ActionEvent evt) -> {
            table.getItems().add(new Student());
        });
        Button save = new Button("حفظ");
        save.setOnAction((evt)->{
            try {
                session.execute(new Unbind(Student.class).from("students"));
                session.execute(new Bind(table.getItems()).to("students"));
            } catch (SofofException ex) {
                ex.printStackTrace();
            }
        });
        BorderPane pane = new BorderPane(table);
        pane.setBottom(new HBox(add, save){{setAlignment(Pos.CENTER);}});
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
