package in.gitbox.cryptonian;

/**
 * Created by sagar on 23-02-2018.
 */

public class Users {

    private String userFirstName;
    private String userSecondName;
    private String phoneNumber;

    public Users(String userFirstName, String userSecondName, String phoneNumber) {
        this.userFirstName = userFirstName;
        this.userSecondName = userSecondName;
        this.phoneNumber = phoneNumber;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public String getUserSecondName() {
        return userSecondName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


}
