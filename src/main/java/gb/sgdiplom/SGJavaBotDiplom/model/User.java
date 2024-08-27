package gb.sgdiplom.SGJavaBotDiplom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.sql.Timestamp;


@Getter
@Entity(name = "usersTable")
public class User {
    @Id
    private Long chatId;

    private String firstName;
    private String lastName;
    private String userName;
    private Timestamp registredAt;


    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRegistredAt(Timestamp registredAt) {
        this.registredAt = registredAt;
    }


    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", registredAt=" + registredAt +
                '}';
    }
}