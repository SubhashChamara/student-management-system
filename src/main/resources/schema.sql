CREATE TABLE IF NOT EXISTS Student(
                                      id VARCHAR(20) PRIMARY KEY ,
                                      name VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS Picture(
                                      student_id VARCHAR(20) PRIMARY KEY ,
                                      picture MEDIUMBLOB NOT NULL ,
                                      CONSTRAINT fk FOREIGN KEY (student_id) REFERENCES Student(id)
);
CREATE TABLE IF NOT EXISTS Attendance(
                                         id INT AUTO_INCREMENT PRIMARY KEY ,
                                         status  ENUM('IN','OUT') NOT NULL ,
                                         stamp DATETIME NOT NULL,
                                         student_id VARCHAR(20)  ,
                                         CONSTRAINT fk1 FOREIGN KEY(student_id) REFERENCES Student(id)
);

CREATE TABLE IF NOT EXISTS User(
                                   user_name VARCHAR(50) PRIMARY KEY ,
                                   full_name VARCHAR(100) NOT NULL ,
                                   password VARCHAR(100) NOT NULL

)

