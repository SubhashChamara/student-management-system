package lk.ijse.dep10.app.models;

import javafx.scene.image.ImageView;

import java.io.Serializable;

public class Student implements Serializable {
    private String id;
    private String name;
    private ImageView imageView;



    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Student(String id, String name, ImageView imageView) {
        this.id = id;
        this.name = name;
        this.imageView = imageView;
    }

    public Student() {
    }
}
