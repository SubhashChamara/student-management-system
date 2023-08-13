package lk.ijse.dep10.app.controlls;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import lk.ijse.dep10.app.db.DBConnection;
import lk.ijse.dep10.app.models.Student;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.*;

public class MainViewController {

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNewStudent;

    @FXML
    private Button btnSave;

    @FXML
    private ImageView profilePicture;

    @FXML
    private TableView<Student> tblStudents;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSearch;

    public void initialize() {
        tblStudents.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("imageView"));
        tblStudents.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudents.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        loadDatabaseData();
        txtName.textProperty().addListener((observableValue, s, current) ->{
            txtName.getStyleClass().remove("invalid");
            if (!current.matches("[A-Za-z ]{3,}")) {
                txtName.getStyleClass().add("invalid");
            }
        } );

        btnDelete.setDisable(true);
        txtSearch.textProperty().addListener((ov, s, current) ->{
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                Statement stm = connection.createStatement();
                String str ="SELECT * FROM Student WHERE name LIKE '%s' OR id LIKE '%s'";
                String sql = String.format(str, "%" + current + "%","%" + current + "%");
                ResultSet rst = stm.executeQuery(sql);
                PreparedStatement stmPicture = connection.prepareStatement("SELECT * FROM Picture WHERE student_id=?");


                tblStudents.getItems().clear();
                while (rst.next()){
                    String id = rst.getString("id");
                    String name = rst.getString("name");
                    stmPicture.setString(1,id);
                    ResultSet rstPic = stmPicture.executeQuery();
                    Student student = new Student(id, name, null);
                    Image image = new Image("/images/no-profile-picture.jpg", 100.00, 100.00, true, true);
                    ImageView imgView = new ImageView(image);
                    student.setImageView(imgView);
                    if (rstPic.next()) {
                        Blob blob = rstPic.getBlob(2);
                        Image profilePic = new Image(blob.getBinaryStream(),100,100,true,true);
                        ImageView imageView = new ImageView(profilePic);
                        student.setImageView(imageView);
                    }
                    tblStudents.getItems().add(student);

                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } );


        tblStudents.getSelectionModel().selectedItemProperty().addListener((ov, item, current) -> {
            for (Node node : new Node[]{btnDelete, btnBrowse, btnSave,txtName}) {
                node.setDisable(current==null);
            }

            if(current==null) return;
            txtId.setText(current.getId());
            txtName.setText(current.getName());
            profilePicture.setImage(current.getImageView().getImage());
            btnClear.setDisable(false);

        });

    }
    private void loadDatabaseData() {
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            Statement stmStudents = connection.createStatement();
            ResultSet rstStudents = stmStudents.executeQuery("SELECT *FROM Student");
            PreparedStatement stmContact = connection.prepareStatement("SELECT *FROM Picture WHERE student_id=?");
            while (rstStudents.next()) {
                String id = rstStudents.getString(1);
                String name = rstStudents.getString(2);
                Student student = new Student(id, name, new ImageView(new Image("/images/no-profile-picture.jpg",100,100,true,false)));
                stmContact.setString(1, id);
                ResultSet rstProfilePicture = stmContact.executeQuery();
                if (rstProfilePicture.next()) {
                    Blob blob = rstProfilePicture.getBlob(2);
                    Image image = new Image(blob.getBinaryStream(),100,100,true,true);
                    ImageView imageView = new ImageView(image);
                    student.setImageView(imageView);
                }
                tblStudents.getItems().add(student);
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load data from the Database").showAndWait();
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpeg", "*.jpg", "*.png"));
        File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
        Image image = new Image(file.toURI().toURL().toString());
        profilePicture.setImage(image);
        btnClear.setDisable(false);
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        Image image = new Image("/images/no-profile-picture.jpg");
        profilePicture.setImage(image);
        btnClear.setDisable(true);
        btnBrowse.requestFocus();
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if(tblStudents.getSelectionModel().getSelectedItem()!=null){
            Student selectStudent = tblStudents.getSelectionModel().getSelectedItem();
            tblStudents.getItems().remove(selectStudent);
            tblStudents.getSelectionModel().clearSelection();
            btnNewStudent.fire();
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                PreparedStatement stm = connection.prepareStatement("DELETE FROM Picture WHERE student_id=?");
                stm.setString(1,selectStudent.getId());
                stm.executeUpdate();
                PreparedStatement stm2 = connection.prepareStatement("DELETE FROM Student WHERE id=?");
                stm2.setString(1,selectStudent.getId());
                stm2.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        for (Node node : new Node[]{txtId, txtName, btnBrowse, btnSave}) {
            if (node instanceof TextField) {
                ((TextField) node).clear();
            }
            node.setDisable(false);
        }
        System.out.println(studentIdGenerate());
        txtId.setText(studentIdGenerate());
        btnClearOnAction(event);
        txtName.requestFocus();
    }

    private String studentIdGenerate() {
        int number = tblStudents.getItems().size() == 0 ? 1 : (Integer.parseInt((tblStudents.getItems().get((tblStudents.getItems().size() - 1)).getId().substring(9))) + 1);
        String id = String.format("DEP-10/S-%03d", number);
        return id;
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String id = txtId.getText();
        String name = txtName.getText();
        if(!isDataValid(name))return;
        Image imageInput = profilePicture.getImage();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageInput, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage,"png",bos);
            byte[] bytes = bos.toByteArray();
            Blob picture = new SerialBlob(bytes);
            Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement stmStudent = connection.prepareStatement("INSERT INTO Student (name, id) VALUES(?,?)");

            if (tblStudents.getSelectionModel().getSelectedItem() != null) {
                stmStudent = connection.prepareStatement("UPDATE Student SET name=? WHERE ID=?");
            }

            stmStudent.setString(1,name);
            stmStudent.setString(2,id);
            stmStudent.executeUpdate();

            if (!btnClear.isDisable()) {
                PreparedStatement stmImage = connection.prepareStatement("INSERT INTO Picture (picture,student_id) VALUES (?,?)");
                if (tblStudents.getSelectionModel().getSelectedItem() != null) {
                    stmImage = connection.prepareStatement("UPDATE Picture SET picture=? WHERE student_id=?");

                }
                System.out.println(picture);
                stmImage.setBlob(1, picture);
                stmImage.setString(2, id);
                System.out.println(stmImage);
                stmImage.executeUpdate();
            }

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to create BLOB profile Picture ,Please contact the technical team").showAndWait();
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SerialException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to create BLOB profile Picture ,Please contact the technical team").showAndWait();
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to store the student in Database please try again...").showAndWait();
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        imageInput.heightProperty().add(100);
        imageInput.widthProperty().add(100);
        Image image = new Image(imageInput.getUrl(), 100, 100, true, true);
        Student newStudent = new Student(id, name, new ImageView(image));
        if(tblStudents.getSelectionModel().getSelectedItem()!=null) tblStudents.getItems()
                .remove(tblStudents.getSelectionModel().getSelectedItem());
        tblStudents.getItems().add(newStudent);
        btnNewStudent.fire();

    }

    private boolean isDataValid(String name) {
        if (!name.matches("[A-Za-z ]{3,}")) {
            txtName.selectAll();
            txtName.requestFocus();
            new Alert(Alert.AlertType.ERROR,"Invalid Name").showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    void tblStudentOnKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            btnDelete.fire();
            tblStudents.getSelectionModel().clearSelection();
        }
    }



}
